package sample.util;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.util.Map;

@Component
public class VelocityEngineSupportImpl implements VelocityEngineSupport {

    //    @Autowired
//    private VelocityEngine velocityEngine;

    @Autowired
    private VelocityEngine velocityDBEngine;

    @Override
    public String mergeTemplate(String templateLocation, Map<String, Object> model) {
        return VelocityEngineUtils.mergeTemplateIntoString(velocityDBEngine, templateLocation, "UTF-8", model);
    }
}
