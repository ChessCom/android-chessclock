package com.mopub.mobileads;

import java.util.HashMap;
import java.util.Map;

class MraidCommandRegistry {
    private static Map<String, MraidCommandFactory> commandMap =
        new HashMap<String, MraidCommandFactory>();
    static {
        commandMap.put("close", new MraidCommandFactory() {
            public MraidCommand create(Map<String, String> params, MraidView view) {
                return new MraidCommandClose(params, view);
            }
        });
        
        commandMap.put("expand", new MraidCommandFactory() {
            public MraidCommand create(Map<String, String> params, MraidView view) {
                return new MraidCommandExpand(params, view);
            }
        });
        
        commandMap.put("usecustomclose", new MraidCommandFactory() {
            public MraidCommand create(Map<String, String> params, MraidView view) {
                return new MraidCommandUseCustomClose(params, view);
            }
        });
        
        commandMap.put("open", new MraidCommandFactory() {
            public MraidCommand create(Map<String, String> params, MraidView view) {
                return new MraidCommandOpen(params, view);
            }
        });
    }
    
    static MraidCommand createCommand(String string, Map<String, String> params, MraidView view) {
        MraidCommandFactory factory = commandMap.get(string);
        return (factory != null) ? factory.create(params, view) : null;
    }
    
    private interface MraidCommandFactory {
        public MraidCommand create(Map<String, String> params, MraidView view);
    }
}
