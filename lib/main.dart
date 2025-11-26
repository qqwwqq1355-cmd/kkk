/// Shopin WebView Mobile App
/// Package: com.jeeey.shopin
/// Version: 1.0.0 (build 3046)
///
/// This app implements a production-ready WebView wrapper for https://m.jeeey.com/tabs/
/// with native features: push notifications, social login bridges, offline support,
/// dynamic theming, and proper back navigation.

import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:webview_flutter/webview_flutter.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:share_plus/share_plus.dart';
import 'package:http/http.dart' as http;

// Brand color constant (maroon)
const Color kBrandMaroon = Color(0xFF7B1B2B);
const Color kAccentWhite = Color(0xFFFFFFFF);
const String kHomeUrl = 'https://m.jeeey.com/tabs/';
const String kThemeEndpoint = 'https://m.jeeey.com/app/theme';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  // Set initial system UI overlay style
  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: kBrandMaroon,
    statusBarIconBrightness: Brightness.light,
    systemNavigationBarColor: kBrandMaroon,
    systemNavigationBarIconBrightness: Brightness.light,
  ));
  runApp(const ShopinApp());
}

class ShopinApp extends StatelessWidget {
  const ShopinApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'جي jeeey',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primaryColor: kBrandMaroon,
        colorScheme: ColorScheme.fromSeed(
          seedColor: kBrandMaroon,
          primary: kBrandMaroon,
        ),
        useMaterial3: true,
      ),
      home: const WebViewScreen(),
    );
  }
}

class WebViewScreen extends StatefulWidget {
  const WebViewScreen({super.key});

  @override
  State<WebViewScreen> createState() => _WebViewScreenState();
}

class _WebViewScreenState extends State<WebViewScreen>
    with SingleTickerProviderStateMixin {
  late WebViewController _controller;
  bool _showSplash = true;
  bool _isOffline = false;
  double _loadProgress = 0.0;
  DateTime? _lastBackPressed;
  Color _themeColor = kBrandMaroon;

  late AnimationController _fadeController;
  late Animation<double> _fadeAnimation;

  // Platform channel for native social login
  static const MethodChannel _socialLoginChannel =
      MethodChannel('com.jeeey.shopin/social_login');

  @override
  void initState() {
    super.initState();
    _fadeController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 500),
    );
    _fadeAnimation = CurvedAnimation(
      parent: _fadeController,
      curve: Curves.easeInOut,
    );
    _initializeApp();
  }

  Future<void> _initializeApp() async {
    await _checkConnectivity();
    if (!_isOffline) {
      _initWebView();
      _fetchThemeFromEndpoint();
    }
  }

  Future<void> _checkConnectivity() async {
    final connectivityResult = await Connectivity().checkConnectivity();
    setState(() {
      // connectivity_plus 5.x returns List<ConnectivityResult>
      _isOffline = connectivityResult.isEmpty || 
                   connectivityResult.contains(ConnectivityResult.none);
    });

    // Listen for connectivity changes
    Connectivity().onConnectivityChanged.listen((result) {
      final wasOffline = _isOffline;
      setState(() {
        _isOffline = result.isEmpty || result.contains(ConnectivityResult.none);
      });
      if (wasOffline && !_isOffline) {
        // Reconnected - reload
        _controller.reload();
      }
    });
  }

  void _initWebView() {
    _controller = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setBackgroundColor(kBrandMaroon)
      ..setNavigationDelegate(
        NavigationDelegate(
          onProgress: (int progress) {
            setState(() {
              _loadProgress = progress / 100.0;
            });
            // Show WebView when progress >= 70% (threshold 65-80%)
            if (progress >= 70 && _showSplash) {
              _fadeController.forward();
              Future.delayed(const Duration(milliseconds: 500), () {
                if (mounted) {
                  setState(() {
                    _showSplash = false;
                  });
                }
              });
            }
          },
          onPageStarted: (String url) {
            // Inject JS to read theme-color meta tag
            _injectThemeReader();
          },
          onPageFinished: (String url) {
            // Inject JS bridges for theme and social login
            _injectJsBridges();
          },
          onWebResourceError: (WebResourceError error) {
            debugPrint('WebView error: ${error.description}');
            if (error.errorType == WebResourceErrorType.hostLookup ||
                error.errorType == WebResourceErrorType.connect) {
              setState(() {
                _isOffline = true;
              });
            }
          },
          onNavigationRequest: (NavigationRequest request) {
            // Security: restrict navigation to safe schemes
            final uri = Uri.parse(request.url);
            if (_isAllowedScheme(uri.scheme)) {
              return NavigationDecision.navigate;
            }
            debugPrint('Blocked navigation to: ${request.url}');
            return NavigationDecision.prevent;
          },
        ),
      )
      ..addJavaScriptChannel(
        'ThemeBridge',
        onMessageReceived: (JavaScriptMessage message) {
          _handleThemeMessage(message.message);
        },
      )
      ..addJavaScriptChannel(
        'SocialLoginBridge',
        onMessageReceived: (JavaScriptMessage message) {
          _handleSocialLoginMessage(message.message);
        },
      )
      ..loadRequest(Uri.parse(kHomeUrl));
  }

  /// Check if URL scheme is allowed for navigation
  bool _isAllowedScheme(String scheme) {
    const allowedSchemes = ['https', 'intent', 'app'];
    return allowedSchemes.contains(scheme.toLowerCase());
  }

  /// Inject JavaScript to read theme-color meta tag
  Future<void> _injectThemeReader() async {
    const js = '''
      (function() {
        var meta = document.querySelector('meta[name="theme-color"]');
        if (meta) {
          ThemeBridge.postMessage(JSON.stringify({type: 'theme', color: meta.content}));
        }
      })();
    ''';
    await _controller.runJavaScript(js);
  }

  /// Inject JavaScript bridges for runtime communication
  Future<void> _injectJsBridges() async {
    const js = '''
      (function() {
        // Listen for theme changes via postMessage
        window.addEventListener('message', function(event) {
          if (event.data && event.data.type === 'theme' && event.data.color) {
            ThemeBridge.postMessage(JSON.stringify(event.data));
          }
          if (event.data && event.data.type === 'social-login' && event.data.provider) {
            SocialLoginBridge.postMessage(JSON.stringify(event.data));
          }
        });
        
        // MutationObserver for theme-color meta changes
        var themeObserver = new MutationObserver(function(mutations) {
          mutations.forEach(function(mutation) {
            if (mutation.target.name === 'theme-color') {
              ThemeBridge.postMessage(JSON.stringify({type: 'theme', color: mutation.target.content}));
            }
          });
        });
        
        var meta = document.querySelector('meta[name="theme-color"]');
        if (meta) {
          themeObserver.observe(meta, {attributes: true, attributeFilter: ['content']});
        }
        
        // Social login button detection patterns
        var googlePatterns = ['Google', 'تسجيل عبر جوجل', 'google', 'جوجل', 'Sign in with Google', 'Continue with Google'];
        var facebookPatterns = ['Facebook', 'تسجيل عبر فيسبوك', 'facebook', 'فيسبوك', 'Sign in with Facebook', 'Continue with Facebook'];
        var googleClasses = ['google-login', 'google-btn', 'google-sign-in', 'btn-google', 'social-google'];
        var facebookClasses = ['facebook-login', 'facebook-btn', 'fb-login', 'btn-facebook', 'social-facebook'];
        
        // Function to check if element matches social login button
        function detectProvider(element) {
          var text = element.textContent || element.innerText || '';
          var className = element.className || '';
          var id = element.id || '';
          
          // Check Google patterns
          if (googlePatterns.some(function(p) { return text.indexOf(p) !== -1; })) return 'google';
          if (googleClasses.some(function(c) { return className.indexOf(c) !== -1 || id.indexOf(c) !== -1; })) return 'google';
          if (id.indexOf('google') !== -1 || className.indexOf('google') !== -1) return 'google';
          
          // Check Facebook patterns
          if (facebookPatterns.some(function(p) { return text.indexOf(p) !== -1; })) return 'facebook';
          if (facebookClasses.some(function(c) { return className.indexOf(c) !== -1 || id.indexOf(c) !== -1; })) return 'facebook';
          if (id.indexOf('facebook') !== -1 || id.indexOf('fb-') !== -1 || className.indexOf('facebook') !== -1 || className.indexOf('fb-') !== -1) return 'facebook';
          
          return null;
        }
        
        // Function to intercept social login buttons
        function interceptSocialButtons(root) {
          var buttons = root.querySelectorAll('button, a, div[role="button"], span[role="button"], input[type="button"], input[type="submit"]');
          buttons.forEach(function(btn) {
            if (btn.dataset.socialIntercepted) return;
            
            var provider = detectProvider(btn);
            if (provider) {
              btn.dataset.socialIntercepted = 'true';
              btn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                SocialLoginBridge.postMessage(JSON.stringify({type: 'social-login', provider: provider}));
                return false;
              }, true);
            }
          });
        }
        
        // Initial scan
        interceptSocialButtons(document);
        
        // MutationObserver to detect dynamically added buttons
        var socialObserver = new MutationObserver(function(mutations) {
          mutations.forEach(function(mutation) {
            mutation.addedNodes.forEach(function(node) {
              if (node.nodeType === 1) {
                interceptSocialButtons(node);
                // Also check the node itself
                var provider = detectProvider(node);
                if (provider && !node.dataset.socialIntercepted) {
                  node.dataset.socialIntercepted = 'true';
                  node.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    SocialLoginBridge.postMessage(JSON.stringify({type: 'social-login', provider: provider}));
                    return false;
                  }, true);
                }
              }
            });
          });
        });
        
        socialObserver.observe(document.body, {childList: true, subtree: true});
      })();
    ''';
    await _controller.runJavaScript(js);
  }

  /// Handle theme change messages from JS bridge
  void _handleThemeMessage(String message) {
    try {
      final data = jsonDecode(message);
      if (data['type'] == 'theme' && data['color'] != null) {
        final colorString = data['color'] as String;
        _applyThemeColor(colorString);
      }
    } catch (e) {
      debugPrint('Error parsing theme message: $e');
    }
  }

  /// Handle social login messages from JS bridge
  void _handleSocialLoginMessage(String message) {
    try {
      final data = jsonDecode(message);
      if (data['type'] == 'social-login' && data['provider'] != null) {
        final provider = data['provider'] as String;
        _triggerNativeSocialLogin(provider);
      }
    } catch (e) {
      debugPrint('Error parsing social login message: $e');
    }
  }

  /// Trigger native social login via platform channel
  /// Implements Google/Facebook sign-in with server binding to backend
  Future<void> _triggerNativeSocialLogin(String provider) async {
    try {
      // This calls native Kotlin code to handle sign-in
      // The native side implements GoogleSignIn/FacebookLogin and returns token
      final result = await _socialLoginChannel.invokeMethod('signIn', {
        'provider': provider,
      });

      if (result != null && result['token'] != null) {
        final token = result['token'] as String;
        
        // Get current URL for return parameter
        final currentUrl = await _controller.currentUrl() ?? kHomeUrl;
        
        // POST token to backend to link session
        try {
          final response = await http.post(
            Uri.parse('https://m.jeeey.com/api/auth/social'),
            headers: {'Content-Type': 'application/json'},
            body: jsonEncode({
              'provider': provider,
              'token': token,
              'return': currentUrl,
            }),
          ).timeout(const Duration(seconds: 10));

          if (response.statusCode == 200) {
            final responseData = jsonDecode(response.body);
            if (responseData['ok'] == true) {
              // Success - reload page to apply session
              await _controller.reload();
              
              // Also notify web page of success
              final script = '''
                window.postMessage({
                  type: 'social-login-result',
                  provider: '$provider',
                  success: true
                }, '*');
              ''';
              await _controller.runJavaScript(script);
            } else {
              throw Exception(responseData['error'] ?? 'Server returned error');
            }
          } else {
            throw Exception('Server error: ${response.statusCode}');
          }
        } catch (e) {
          debugPrint('Backend auth error: $e');
          _showLoginError('حدث خطأ في الاتصال بالخادم');
        }
      }
    } on PlatformException catch (e) {
      debugPrint('Social login error: ${e.message}');
      // Show error to user
      if (e.code == 'CANCELLED') {
        // User cancelled - no need to show error
        return;
      }
      _showLoginError(e.message ?? 'حدث خطأ في تسجيل الدخول');
    }
  }
  
  /// Show login error snackbar
  void _showLoginError(String message) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(message),
          backgroundColor: Colors.red.shade700,
          duration: const Duration(seconds: 3),
        ),
      );
    }
  }

  /// Fetch theme from REST endpoint
  Future<void> _fetchThemeFromEndpoint() async {
    try {
      final response = await http.get(Uri.parse(kThemeEndpoint)).timeout(
        const Duration(seconds: 5),
        onTimeout: () => http.Response('', 408),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        if (data['primary'] != null) {
          _applyThemeColor(data['primary']);
        }
      }
    } catch (e) {
      debugPrint('Theme fetch error (using default): $e');
      // Use default brand color if fetch fails
    }
  }

  /// Apply theme color to system bars
  void _applyThemeColor(String colorHex) {
    try {
      // Parse hex color (supports #RGB, #RRGGBB, #AARRGGBB)
      String hex = colorHex.replaceFirst('#', '');
      if (hex.length == 3) {
        hex = hex.split('').map((c) => '$c$c').join();
      }
      if (hex.length == 6) {
        hex = 'FF$hex';
      }
      final color = Color(int.parse(hex, radix: 16));

      setState(() {
        _themeColor = color;
      });

      // Determine if color is light or dark for icon brightness
      final luminance = color.computeLuminance();
      final brightness = luminance > 0.5 ? Brightness.dark : Brightness.light;

      SystemChrome.setSystemUIOverlayStyle(SystemUiOverlayStyle(
        statusBarColor: color,
        statusBarIconBrightness: brightness,
        systemNavigationBarColor: color,
        systemNavigationBarIconBrightness: brightness,
      ));
    } catch (e) {
      debugPrint('Error applying theme color: $e');
    }
  }

  /// Share current URL
  Future<void> _shareCurrentUrl() async {
    final url = await _controller.currentUrl();
    if (url != null && url.isNotEmpty) {
      await Share.share(url, subject: 'Check out this page');
    }
  }

  /// Handle back button with double-tap to exit
  Future<bool> _handleBackPress() async {
    if (_isOffline) {
      return true; // Allow exit if offline
    }

    if (await _controller.canGoBack()) {
      await _controller.goBack();
      return false;
    } else {
      // Double back to exit logic
      final now = DateTime.now();
      if (_lastBackPressed != null &&
          now.difference(_lastBackPressed!) < const Duration(seconds: 2)) {
        return true; // Exit app
      }

      _lastBackPressed = now;

      // Show snackbar
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: const Text(
              'Press again to exit',
              style: TextStyle(color: Colors.white),
            ),
            backgroundColor: kBrandMaroon.withOpacity(0.9),
            duration: const Duration(seconds: 2),
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
      return false;
    }
  }

  /// Retry loading when offline
  void _retryLoad() {
    setState(() {
      _isOffline = false;
      _showSplash = true;
      _loadProgress = 0.0;
    });
    _initializeApp();
  }

  @override
  void dispose() {
    _fadeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (bool didPop, dynamic result) async {
        if (didPop) return;
        final shouldPop = await _handleBackPress();
        if (shouldPop) {
          // Use SystemNavigator.pop() for proper app exit
          SystemNavigator.pop();
        }
      },
      child: Scaffold(
        body: Stack(
          children: [
            // Offline fallback screen
            if (_isOffline)
              _buildOfflineScreen()
            // WebView (hidden during splash)
            else
              AnimatedOpacity(
                opacity: _showSplash ? 0.0 : 1.0,
                duration: const Duration(milliseconds: 500),
                child: WebViewWidget(controller: _controller),
              ),

            // Splash screen overlay
            if (_showSplash && !_isOffline) _buildSplashScreen(),
          ],
        ),
        floatingActionButton: (!_showSplash && !_isOffline)
            ? FloatingActionButton(
                onPressed: _shareCurrentUrl,
                backgroundColor: _themeColor,
                child: const Icon(Icons.share, color: Colors.white),
              )
            : null,
      ),
    );
  }

  Widget _buildSplashScreen() {
    return Container(
      color: kBrandMaroon,
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Logo with circular progress indicator
            Stack(
              alignment: Alignment.center,
              children: [
                SizedBox(
                  width: 180,
                  height: 180,
                  child: CircularProgressIndicator(
                    value: _loadProgress,
                    strokeWidth: 4,
                    valueColor:
                        const AlwaysStoppedAnimation<Color>(kAccentWhite),
                    backgroundColor: kAccentWhite.withOpacity(0.3),
                  ),
                ),
                // Logo placeholder - shows app icon
                Container(
                  width: 140,
                  height: 140,
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(20),
                    child: Image.asset(
                      'assets/logo.png',
                      width: 140,
                      height: 140,
                      fit: BoxFit.contain,
                      errorBuilder: (context, error, stackTrace) {
                        // Fallback if logo.png not found
                        return const Icon(
                          Icons.shopping_bag,
                          size: 80,
                          color: kBrandMaroon,
                        );
                      },
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 30),
            Text(
              'Loading... ${(_loadProgress * 100).toInt()}%',
              style: const TextStyle(
                color: Colors.white,
                fontSize: 16,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildOfflineScreen() {
    return Container(
      color: kBrandMaroon,
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(32.0),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(
                Icons.wifi_off,
                size: 80,
                color: Colors.white,
              ),
              const SizedBox(height: 24),
              const Text(
                'No Internet Connection',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 12),
              const Text(
                'Please check your connection and try again.',
                style: TextStyle(
                  color: Colors.white70,
                  fontSize: 16,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 32),
              ElevatedButton.icon(
                onPressed: _retryLoad,
                icon: const Icon(Icons.refresh),
                label: const Text('Retry'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.white,
                  foregroundColor: kBrandMaroon,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 32,
                    vertical: 16,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
