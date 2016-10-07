/********* PPCInAppBrowser.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <WebKit/WebKit.h>
@interface PPCInAppBrowser : CDVPlugin <WKNavigationDelegate,WKUIDelegate,UIWebViewDelegate> {
        // Member variables go here.
    UIViewController *vc;
    UIActivityIndicatorView *activityIndicator;
}
@property(nonatomic,strong) WKWebView *wkWebView;
@property(nonatomic,strong) UIToolbar *toolBar;
@property(nonatomic,strong) UIBarButtonItem *doneBarButton;
@property(nonatomic,strong) UIBarButtonItem *backbarButton;
@property(nonatomic,strong) UIBarButtonItem *forwardbarButton;
- (void)coolMethod:(CDVInvokedUrlCommand*)command;
- (void)openBrowser:(CDVInvokedUrlCommand*)command;
@end

@implementation PPCInAppBrowser

- (void)coolMethod:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];

    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
- (void)openBrowser:(CDVInvokedUrlCommand*)command
{
    NSString* options = [command.arguments objectAtIndex:0];

    vc = [[UIViewController alloc]init];
    vc.view.backgroundColor=[UIColor whiteColor];
    UIDeviceOrientation orientation = [[UIDevice currentDevice] orientation];
    CGRect frame;
    frame = vc.view.frame;
    if (orientation == UIDeviceOrientationPortrait)
      {
        frame = vc.view.frame;
      }
    else if(orientation == UIDeviceOrientationLandscapeLeft || orientation == UIDeviceOrientationLandscapeRight){
        frame.size.width = vc.view.frame.size.height;
        frame.size.height = vc.view.frame.size.width;
    }
    [[NSNotificationCenter defaultCenter] addObserver:self  selector:@selector(orientationChanged:)    name:UIDeviceOrientationDidChangeNotification  object:nil];
    vc.view.frame = frame;


    WKWebViewConfiguration *theConfiguration = [[WKWebViewConfiguration alloc] init];
    self.wkWebView = [[WKWebView alloc] initWithFrame:CGRectMake(0, 20, vc.view.frame.size.width, vc.view.frame.size.height-44) configuration:theConfiguration];
    self.wkWebView.navigationDelegate = self;
	self.wkWebView.UIDelegate = self;
    NSURL *nsurl=[NSURL URLWithString:options];
    NSURLRequest *nsrequest=[NSURLRequest requestWithURL:nsurl];
    [self.wkWebView loadRequest:nsrequest];
    [vc.view addSubview:self.wkWebView];

    self.backbarButton.enabled=self.wkWebView.canGoBack;
    self.forwardbarButton.enabled=self.wkWebView.canGoForward;


    activityIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite];
    activityIndicator.alpha = 1.0;
    activityIndicator.center = vc.view.center;
    activityIndicator.color=[UIColor colorWithRed:26.0/255.0 green:152.0/255.0 blue:252.0/255.0 alpha:1.0];
    activityIndicator.hidesWhenStopped = YES;
    [vc.view addSubview:activityIndicator];
    [activityIndicator startAnimating];

    self.toolBar=[[UIToolbar alloc]initWithFrame:CGRectMake(0, vc.view.frame.size.height-44, vc.view.frame.size.width, 44)];
    [vc.view addSubview:self.toolBar];
    self.toolBar.barTintColor=[UIColor whiteColor];

    self.backbarButton=[[UIBarButtonItem alloc]initWithBarButtonSystemItem:UIBarButtonSystemItemRewind target:self action:@selector(backButtonPressed:)];

    UIBarButtonItem *spacebarButton=[[UIBarButtonItem alloc]initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:self action:nil];

    UIBarButtonItem *spacebarfixedButton=[[UIBarButtonItem alloc]initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:self action:nil];


    self.forwardbarButton=[[UIBarButtonItem alloc]initWithBarButtonSystemItem:UIBarButtonSystemItemFastForward target:self action:@selector(frowardPressed)];

    self.doneBarButton=[[UIBarButtonItem alloc]initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(dismis)];
    [self.toolBar setItems:@[self.doneBarButton,spacebarButton,self.backbarButton,spacebarfixedButton,self.forwardbarButton]];

    self.backbarButton.enabled=self.wkWebView.canGoBack;
    self.forwardbarButton.enabled=self.wkWebView.canGoForward;

    [self.viewController presentViewController:vc animated:YES completion:nil];
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
- (IBAction)backButtonPressed:(id)sender {
    if([self.wkWebView canGoBack])
      {
        [self.wkWebView goBack];
      }

}
- (void)orientationChanged:(NSNotification *)notification{
    self.wkWebView.frame = CGRectMake(0, 20, vc.view.frame.size.width, vc.view.frame.size.height-64) ;
    self.toolBar.frame=CGRectMake(0, vc.view.frame.size.height-44, vc.view.frame.size.width, 44);
    activityIndicator.center = vc.view.center;
}
-(void)frowardPressed
{
    [self.wkWebView goForward];;
}
- (void)dismis{
    [self.viewController dismissViewControllerAnimated:YES completion:NULL];
}
- (void)webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation;
{
    [activityIndicator startAnimating];
    self.backbarButton.enabled=webView.canGoBack;
    self.forwardbarButton.enabled=self.wkWebView.canGoForward;

}
- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation;
{
    [activityIndicator stopAnimating];
    self.backbarButton.enabled=webView.canGoBack;

}
- (void)webView:(WKWebView *)webView didFailProvisionalNavigation:(null_unspecified WKNavigation *)navigation withError:(NSError *)error{
    [activityIndicator startAnimating];
    self.backbarButton.enabled=webView.canGoBack;
    self.forwardbarButton.enabled=self.wkWebView.canGoForward;
}

- (void)webView:(WKWebView *)webView didCommitNavigation:(null_unspecified WKNavigation *)navigation{
        //    [activityIndicator stopAnimating];
        //    self.backbarButton.enabled=webView.canGoBack;
        //    self.forwardbarButton.enabled=self.wkWebView.canGoForward;
}

- (void)webView:(WKWebView *)webView didFailNavigation:(null_unspecified WKNavigation *)navigation withError:(NSError *)error{
    [activityIndicator stopAnimating];
    self.backbarButton.enabled=webView.canGoBack;
    self.forwardbarButton.enabled=self.wkWebView.canGoForward;
}


- (void)dealloc {
    [activityIndicator stopAnimating];
}

- (void)webView:(WKWebView *)webView runJavaScriptAlertPanelWithMessage:(NSString *)message initiatedByFrame:(WKFrameInfo *)frame completionHandler:(void (^)(void))completionHandler
{
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:message
                                                                             message:nil
                                                                      preferredStyle:UIAlertControllerStyleAlert];
    [alertController addAction:[UIAlertAction actionWithTitle:@"OK"
                                                        style:UIAlertActionStyleCancel
                                                      handler:^(UIAlertAction *action) {
                                                          completionHandler();
                                                      }]];
    [vc presentViewController:alertController animated:YES completion:^{}];
}
@end
