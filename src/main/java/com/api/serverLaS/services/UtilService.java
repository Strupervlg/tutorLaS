package com.api.serverLaS.services;

import its.model.definition.DomainModel;
import its.model.definition.ObjectDef;
import its.model.definition.ObjectRef;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UtilService {

    private Map<String, String> enumToString = Map.of(
            "positionExpression:right", "правым",
            "positionExpression:left", "левым",
            "positionExpression:center", "центральным",
            "typeVariable:input", "входной",
            "typeVariable:output", "выходной",
            "typeVariable:mutable", "изменяемой"
    );

    private Map<String, String> enumToStringI = Map.of(
            "typeVariable:input", "входная",
            "typeVariable:output", "выходная",
            "typeVariable:mutable", "изменяемая"
    );

    public String generateMessage(String template, Map<String, ObjectRef> situation, DomainModel domain) {
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            Pattern pattern1 = Pattern.compile("->|\\.|[\\w)(]+|\\$\\w+");
            Matcher matcher1 = pattern1.matcher(matcher.group(1));

            ArrayList<String> tokens = new ArrayList<>();

            while (matcher1.find()) {
                tokens.add(matcher1.group());
            }

            String[] array = tokens.toArray(new String[0]);
            ObjectDef obj;
            if(array[0].charAt(0) == '$') {
                obj = domain.getObjects().get(array[0].substring(1));
            } else {
                obj = situation.get(array[0]).findIn(domain);
            }

            String value = "";
            boolean isI = false;
            for (int i = 1; i < array.length; i += 2) {
                if(array[i].equals("->")) {
                    int nextIndex = i+1;
                    obj = domain.getObjects().get(
                            obj.getRelationshipLinks().stream().filter(
                                    relationshipLinkStatement -> relationshipLinkStatement.getRelationshipName().equals(array[nextIndex])
                            ).findFirst().orElse(null).getObjectNames().get(0)
                    );
                } else if (array[i].equals(".")) {
                    if(array[i+1].contains("(I)")) {
                        String property = array[i+1].replace("(I)", "");
                        isI = true;
                        value = obj.getPropertyValue(property).toString();
                    } else {
                        value = obj.getPropertyValue(array[i+1]).toString();
                    }
                }
            }
            if(isI && enumToStringI.get(value) != null) {
                value = enumToStringI.get(value);
            } else if(!isI && enumToString.get(value) != null) {
                value = enumToString.get(value);
            }
            matcher.appendReplacement(result, value);
        }
        matcher.appendTail(result);
        if (!result.isEmpty()) {
            // Увеличиваем первую букву
            char firstChar = Character.toUpperCase(result.charAt(0));
            result.setCharAt(0, firstChar);
        }
        return result.toString();
    }
}
