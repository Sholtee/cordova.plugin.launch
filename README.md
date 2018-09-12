# Launch (PhoneGap/Cordova Plugin)

This plugin lets you launch apps (identified by package name) by implementing `window.launch()` method. Unlike similar plugins here you can read custom data (passed on terminate) back from launched app.

### Platform Support

This plugin supports PhoneGap/Cordova apps running on Android.

## Installation

#### Automatic Installation using PhoneGap/Cordova CLI (Android)

`cordova plugin add @sholtee/cordova-plugin-launch`

## Usage
```js
const result = await window.launch({
    packageName: "com.foo.bar", // required
    extras: { // optional
        ...
    }
});

const extras = result.extras;
if (extras.RetVal) {  // if you're using @sholtee/cordova-plugin-exit
    ...
}
```