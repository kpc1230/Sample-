package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by aliakseimatsarski on 9/1/17.
 */
public class VariableSetTypeConverter implements DynamoDBTypeConverter<Set<String>, Set<Variable>> {

    private static Logger log = LoggerFactory.getLogger("application");

    @Override
    public Set<String> convert(Set<Variable> variables) {
        if (variables == null){
            return null;
        }
        Set<String> result = new TreeSet<>();
        for (Variable variable:variables){
            result.add(variable.toJson().toString());
        }
        return result;
    }

    @Override
    public Set<Variable> unconvert(Set<String> varStringList) {
        Set<Variable> result = new TreeSet<>();
        ObjectMapper om = new ObjectMapper();
        for (String jsonStr:varStringList){
            try{
                Variable variable = om.readValue(jsonStr, Variable.class);
                result.add(variable);
            } catch (IOException e) {
                log.error("Error during parsing Tag object.", e);
            }
        }
        return result;
    }
}
