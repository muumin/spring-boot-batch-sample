package sample.util;

import java.util.Map;

public interface VelocityEngineSupport {
    String mergeTemplate(String templateLocation, Map<String, Object> model);
}
