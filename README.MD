
usage
```
var keyDef=[{keyCode:39,scanCode:37,needPropagate:false,name:"ctrl"},{keyCode:29,scanCode:30,needPropagate:false,name:"ctrl"}];
NativeModules.KeyEventModule.keyEventDef(JSON.stringify(keyDef));
DeviceEventEmitter.addListener('keyDown', function(e) {
// handle event.
// alert("click");
});
DeviceEventEmitter.addListener('keyUp', function(e) {
// handle event.
// alert("click");
});
}
```
goto https://www.beecode.cn/ for more info.
