package android.webkit;

/*
 * Starting with API 17, calls to WebView#addJavascriptInterface must be marked with a
 * @JavascriptInterface annotation. Unfortunately, the annotation itself is only available on API
 * 17+, so we cheat by making a copy for our own use.
 *
 * http://developer.android.com/reference/android/webkit/WebView.html#addJavascriptInterface(java.lang.Object, java.lang.String)
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
public @interface JavascriptInterface {
}
