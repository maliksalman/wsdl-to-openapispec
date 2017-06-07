package com.smalik.wsdl2yamlspec;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.WSDLToJava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class WsdlToYamlSpec {

    private static Logger logger = LoggerFactory.getLogger(WsdlToYamlSpec.class);

    public static void main(String[] args) {

        if (args.length != 3) {

            System.err.println("Usage: WsdlToYamlSpec <hosted-wsdl-url> <root-nameclass-pairs> <output-yaml-file>");
            System.err.println("\nExample arguments:\n");
            System.err.println("     hosted-wsdl-url:      http://hostname:port/path/to/wsdl/location/sample.wsdl");
            System.err.println("     root-nameclass-pairs: Request:com.sample.MyRequestClass,Response:com.sample.MyResponseClass,...,SomethingElse:fully.qualified.ClassName");
            System.err.println("     output-yaml-file:     spec.yaml");
            System.exit(1);
        }

        try {
            // get the WSDL url
            String wsdlUrl = args[0];

            // parse the name-class pairs
            List<NameClassPair> pairs = new ArrayList<>();
            for (String pairString: StringUtils.split(args[1], ',')) {
                String[] pairParts = StringUtils.split(pairString, ':');
                pairs.add(new NameClassPair(pairParts[0], pairParts[1]));
            }

            // get the output filename
            String outputYaml = args[2];

            new WsdlToYamlSpec().convert(wsdlUrl, new File(outputYaml), pairs);
        } catch (Exception e) {
            logger.error("Something went wrong", e);
        }
    }

    public void convert(String wsdlUri, File outputYaml, List<NameClassPair> pairs) throws Exception {
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