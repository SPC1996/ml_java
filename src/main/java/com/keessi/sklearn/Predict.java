package com.keessi.sklearn;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Classification;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.PMMLUtil;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Predict {
    private Evaluator evaluator;

    public void loadEvaluator(String pmmlpath) throws IOException, JAXBException, SAXException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(pmmlpath));
        ZipEntry zipEntry = zis.getNextEntry();
        ZipFile zipFile = new ZipFile(pmmlpath);
        PMML pmml = PMMLUtil.unmarshal(zipFile.getInputStream(zipEntry));
        zis.close();
        ModelEvaluatorFactory factory = ModelEvaluatorFactory.newInstance();
        evaluator = factory.newModelEvaluator(pmml);
    }

    public String evaluate(double[] input) {
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
        List<InputField> inputFields = evaluator.getInputFields();
        for (InputField inputField : inputFields) {
            FieldName inputFieldName = inputField.getName();
            int index = Integer.parseInt(inputFieldName.getValue().substring(1)) - 1;
            FieldValue inputFieldValue = inputField.prepare(input[index]);
            arguments.put(inputFieldName, inputFieldValue);
        }
        Map<FieldName, ?> results = evaluator.evaluate(arguments);
        return ((Classification) results.get(new FieldName("y"))).getResult().toString();
    }

    public List<String> evaluate(double[][] inputs) {
        List<String> results = new ArrayList<>();
        for (double[] input : inputs) {
            results.add(evaluate(input));
        }
        return results;
    }

    public boolean validate(String output, String target) {
        return output.equals(target);
    }

    public boolean validate(List<String> outputs, String target, double threshold) {
        int trueNum = 0, falseNum = 0;
        for (String output : outputs) {
            if (validate(output, target)) {
                trueNum += 1;
            } else {
                falseNum += 1;
            }
        }
        return (trueNum * 1.0 / (trueNum + falseNum)) >= threshold;
    }
}
