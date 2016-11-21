


/**
 * Created by airyuxun on 16/3/16.
 */
public class NectarActivity extends ReactActivity{

 

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {


        for(NectarEventListener listener:listeners){

            if(listener!=null){
                KeyEventAdapter keyEventAdapter = listener.getAdapter(KeyEventAdapter.class);
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if(keyEventAdapter == null){
                        WritableMap eventMap = Arguments.createMap();
                        eventMap.putString("name","kewDown");
                        eventMap.putInt("keyCode",event.getKeyCode());
                        eventMap.putInt("scanCode",event.getScanCode());
                        eventMap.putInt("metaState",event.getMetaState());
                        listener.onEvent("keyDown",eventMap);
                    }else{
                        if(keyEventAdapter.onKeyUp(event.getKeyCode(), event)){
                            return true;
                        }else{
                            return super.dispatchKeyEvent(event);
                        }
                    }
                }else if(event.getAction() == KeyEvent.ACTION_UP){

                    if(keyEventAdapter == null){
                        JavaOnlyMap eventMap = new JavaOnlyMap();
                        eventMap.putString("name","keyUp");
                        eventMap.putInt("scanCode",event.getScanCode());
                        eventMap.putInt("metaState",event.getMetaState());
                        eventMap.putInt("keyCode",event.getKeyCode());
                        listener.onEvent("keyUp",eventMap);
                    }else {
                         if(keyEventAdapter.onKeyUp(event.getKeyCode(), event)){
                            return true;
                         }else{
                             return super.dispatchKeyEvent(event);
                         }
                    }

                }

            }
        }
        return super.dispatchKeyEvent(event);
    }


}
