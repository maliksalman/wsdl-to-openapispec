package com.smalik.wsdl2yamlspec;

import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.WSDLToJava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;

public class WsdlToYamlSpec {

    private static Logger logger = LoggerFactory.getLogger(WsdlToYamlSpec.class);

    public static void main(String[] args) {

        try {
            WsdlToYamlSpec converter = new WsdlToYamlSpec();
            NameClassPair[] pairs = new NameClassPair[] {
                    new NameClassPair("DetermineRiskAndPremiumsRequest", "com.lmig.pi.vehicleratingmarketservice.messages.DetermineRiskAndPremiumsVehicleAgreementRequestDTO"),
                    new NameClassPair("DetermineRiskAndPremiumsResponse", "com.lmig.pi.vehicleratingmarketservice.messages.DetermineRiskAndPremiumsResponse")
            };
            converter.convert("http://axpin-p6xo0dnb.lmig.com:9080/PiVehicleRatingUnderwritingMarketServiceModuleWeb/sca/VehicleRatingMarketServiceWS/WEB-INF/wsdl/com/lmig/pi/vehicleratingmarketservice/PiVehicleRatingUnderwritingMarketServiceModule_VehicleRatingMarketServiceWS.wsdl", new File("spec.yaml"), pairs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void convert(String wsdlUri, File outputYaml, NameClassPair... pairs) throws Exception {
        URLClassLoader newCl = getGeneratedClassLoader(wsdlUri);
        YamlGenerator generator = new YamlGenerator(newCl);

        logger.info("Writing spec definitions to path: " + outputYaml.getAbsolutePath());
        try (PrintWriter out = new PrintWriter(outputYaml)) {
            out.println(generator.generateYaml(pairs));
        }
    }

    private URLClassLoader getGeneratedClassLoader(String wsdlUri) throws Exception {
        // create the temp dir with sources and classes dirs
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "wsdl2java-" + System.currentTimeMillis());

        File sources = new File(tempDir, "sources");
        sources.mkdirs();

        File classes = new File(tempDir, "classes");
        classes.mkdirs();

        logger.info("Generating classes: Sources=" + sources.getAbsolutePath() + ", Classes=" + classes.getAbsolutePath());

        String[] args = {
                "-d",
                sources.getAbsolutePath(),
                "-compile",
                "-classdir",
                classes.getAbsolutePath(),
                wsdlUri
        };

        WSDLToJava wsdlToJava = new WSDLToJava(args);
        wsdlToJava.run(new ToolContext());

        return new URLClassLoader(new URL[] { classes.toURI().toURL() }, getClass().getClassLoader());
    }
}