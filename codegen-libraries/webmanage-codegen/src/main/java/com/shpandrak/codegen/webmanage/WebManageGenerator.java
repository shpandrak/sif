package com.shpandrak.codegen.webmanage;

import com.shpandrak.codegen.BaseGenerator;
import com.shpandrak.codegen.PojoGenerator;
import com.shpandrak.codegen.model.*;
import com.shpandrak.codegen.rest.RestLayerGenerator;
import com.shpandrak.codegen.xml.XMLConvertersGenerator;
import com.shpandrak.common.string.StringUtil;
import com.shpandrak.metadata.generator.GenerationContext;
import com.shpandrak.metadata.generator.IMetadataGenerator;
import com.shpandrak.metadata.generator.MetadataGeneratorException;
import com.shpandrak.metadata.model.DataEntityMetadata;
import com.shpandrak.metadata.model.MetadataStore;
import com.shpandrak.metadata.model.field.AbstractFieldMetadata;
import com.shpandrak.metadata.model.field.EnumFieldDef;
import com.shpandrak.metadata.model.relation.AbstractRelation;
import com.shpandrak.web.manage.BaseWebmanageServlet;
import com.shpandrak.xml.EntityXMLConverter;
import com.shpandrak.xml.XMLConverter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/13/12
 * Time: 09:42
 */
public class WebManageGenerator extends BaseGenerator {
    private static final Logger logger = LoggerFactory.getLogger(WebManageGenerator.class);
    Map<String, StringBuilder> generatedJSResources = new HashMap<String, StringBuilder>();


    @Override
    protected void generateClasses(MetadataStore store, GeneratedPackage generatedPackage, GenerationContext generationContext) throws MetadataGeneratorException {
        RestLayerGenerator restLayerGenerator = getGenerator(RestLayerGenerator.class, generationContext);
        XMLConvertersGenerator xmlGenerator = getGenerator(XMLConvertersGenerator.class, generationContext);

        GeneratedPackage generatedPojosPackage = pojoGenerator.getGeneratedPackage();
        GeneratedPackage generatedRestResourcesPackage = restLayerGenerator.getGeneratedPackage();

        for (DataEntityMetadata currEntity : store.getEntities()) {
            GeneratedClass currPojo = generatedPojosPackage.getClass(currEntity.getJavaClassName());
            GeneratedClass currRestResource = generatedRestResourcesPackage.getClass(RestLayerGenerator.getResourceName(currPojo));

            GeneratedClass webManager = generateWebManageServlet(currEntity, currPojo, currRestResource);
            generatedPackage.addClass(webManager);

        }

    }

    @Override
    protected GeneratedPackage createPackage(MetadataStore store) {
        return new GeneratedPackage(store.getNamespace() + ".web.manage");
    }

    private GeneratedClass generateWebManageServlet(DataEntityMetadata currEntity, GeneratedClass currPojo, GeneratedClass currRestResource) throws MetadataGeneratorException {
        GeneratedClass c = new GeneratedClass(currPojo.getName() + "WebManageServlet", getGeneratedPackage().getName());
        c.addAnnotation(new GeneratedAnnotation(new ClassGeneratedType(WebServlet.class), Arrays.asList(new GeneratedCodeLine("\"/webmanage/" + currEntity.getName() + "\""))));
        c.setExtendsClass(BaseWebmanageServlet.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo))));

        generateGetClassTypeMethod(currPojo, c);

        generateGetXMLConverterMethod(currPojo, c);

        generateRootResourcePathMethod(currEntity, c, currRestResource);

        generateJSLibrary(currEntity, c);

        generateIncludeJSLibrariesMethod(currEntity, c);

        generateGetEntityClassLibraryName(currEntity, c);




        return c;
    }

    private void generateJSLibrary(DataEntityMetadata currEntity, GeneratedClass c) throws MetadataGeneratorException {
        StringBuilder sb = new StringBuilder();
        generatedJSResources.put(generateJSFileName(currEntity), sb);
        String libraryName = generateLibraryName(currEntity);
        sb.append("var ").append(libraryName).append(" = {\n");
        generateFillTableFunction(currEntity, sb, libraryName);
        sb.append(",\n\n");
        generateCreateNewEntityFunction(currEntity, sb, libraryName);
        sb.append(",\n\n");
        generateDeleteEntityFunction(currEntity, sb, libraryName);
        sb.append(",\n\n");
        generateListFunction(currEntity, sb, libraryName);
        sb.append(",\n\n");
        generateCreateEntityHTMLSelectFunction(currEntity, sb, libraryName);
        sb.append("\n};\n");


    }

    private void generateCreateEntityHTMLSelectFunction(DataEntityMetadata currEntity, StringBuilder sb, String libraryName) {
        String entityTagName = XMLConvertersGenerator.generateRootTagName(currEntity);
        sb.append(
                "createEntityHTMLSelectFunction : function(xml, generatedFieldId, allowBlank){\n").append(
            "    var selectHtml = \"<select id=\\\"\" + generatedFieldId + \"\\\">\";\n").append(
                "    if (allowBlank) {\n").append(
            "        selectHtml += \"<option value=\\\"null\\\">[None]</option>\";\n").append(
            "    }\n").append(
            "    $(xml).find(\"").append(entityTagName).append("\").each(function () {\n").append(
            "         var currEntity = $(this);\n").append(
            "         selectHtml += \"<option value=\\\"\" + currEntity.attr(\"id\") + \"\\\">\" +\n");

        boolean first = true;
        for (AbstractFieldMetadata currField : currEntity.getFields()){

            if (first){
                first = false;
                sb.append("             ");
            }else {
                sb.append("             \"-\" +");
            }
            sb.append(
                    "currEntity.find(\"").append(currField.getName()).append("\").text() +\n");
        }
        sb.append("           \"</option>\";\n").append(
                  "    });\n").append(
                  "    selectHtml += \"</select>\";\n").append(
                  "    return selectHtml;\n").append(
                  "}\n");
    }

    private void generateListFunction(DataEntityMetadata currEntity, StringBuilder sb, String libraryName) {
        String entityResourcePath = RestLayerGenerator.getResourcePath(currEntity);

        sb.append("list : function(successCallback, failureCallback){\n").append(
                "    log(\"get all ").append(currEntity.getName()).append("\");\n").append(
                "    new Ajax.Request(\"../rest/" + entityResourcePath + "\", {\n" +
                "        method: 'get',\n" +
                "        evalJS: false,\n" +
                "        onSuccess: function(transport) {\n" +
                "            log(\"fetch all ").append(currEntity.getName()).append(" successfuly\");\n").append(
                "            successCallback(transport.responseText);\n" +
                        "        },\n" +
                        "        onFailure: function(transport) {\n" +
                        "            log(\"Failed fetching all\" + transport.status + \": \" + transport.statusText);\n" +
                        "            alert(\"Failed fetching all\" + transport.status + \": \" + transport.statusText);\n" +
                        "            failureCallback(\"Failed fetching all\" + transport.status + \": \" + transport.statusText);\n" +
                        "        },\n" +
                        "        onException: function(request, e) {\n" +
                        "            log(\"Exception occurred fetching all \" + e);\n" +
                        "            alert(\"Exception occurred fetching all \" + e);\n" +
                        "            failureCallback(\"Exception occurred fetching all \" + e);\n" +
                        "        }\n" +
                        "    });\n" +
                        "}\n");
    }

    private void generateIncludeJSLibrariesMethod(DataEntityMetadata currEntity, GeneratedClass c) {
        String code = "return new " + HashSet.class.getSimpleName() + "<String>(" + Arrays.class.getSimpleName() + ".asList(\"" + generateJSFileName(currEntity) + "\"";
        for (AbstractRelation currRelationship : currEntity.getRelations()){
            code += ", \"" + generateJSFileName(store.getEntityMetadataByName(currRelationship.getRelatedEntity())) + "\"";
        }
        code += "));";

        GeneratedCodeLine generatedCodeLine = new GeneratedCodeLine(code, Arrays.asList(new ClassGeneratedType(HashSet.class), new ClassGeneratedType(Arrays.class)));

        c.addMethod(new GeneratedMethod("getIncludeJSLibraries", GeneratedModifier.PROTECTED, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(Set.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new GeneratedType("String")))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(generatedCodeLine))));
    }

    private void generateGetEntityClassLibraryName(DataEntityMetadata currEntity, GeneratedClass c) {
        String code = "return \"" + generateLibraryName(currEntity) + "\";";

        GeneratedCodeLine generatedCodeLine = new GeneratedCodeLine(code, Arrays.asList(new ClassGeneratedType(HashSet.class), new ClassGeneratedType(Arrays.class)));

        c.addMethod(new GeneratedMethod("getEntityClassLibraryName", GeneratedModifier.PROTECTED, Collections.<GeneratedParameter>emptyList(), new GeneratedType("String"), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(generatedCodeLine))));
    }

    private void generateGetXMLConverterMethod(GeneratedClass currPojo, GeneratedClass c) {
        ArrayList<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
        lines.add(new GeneratedCodeLine("return new " + EntityXMLConverter.class.getSimpleName() + "<" + currPojo.getName() + ">(" + currPojo.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + ");", Arrays.asList(new ClassGeneratedType(currPojo), new ClassGeneratedType(EntityXMLConverter.class))));
        c.addMethod(new GeneratedMethod("getXMLConverter", GeneratedModifier.PROTECTED, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(XMLConverter.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(lines)));
    }

    private void generateFillTableFunction(DataEntityMetadata currEntity, StringBuilder sb, String libraryName) throws MetadataGeneratorException {

        String entityTagName = XMLConvertersGenerator.generateRootTagName(currEntity);

        String titleRowHTML = "<tr><td><input type=\\\"checkbox\\\"/></td>";
        for (AbstractFieldMetadata currField : currEntity.getFields()){
            titleRowHTML += "<td>" + currField.getName() + "</td>";
        }

        for (AbstractRelation currRelationship : currEntity.getRelations()){
            titleRowHTML += "<td>" + currRelationship.getRelationshipName() + "</td>";
        }

        titleRowHTML += "</tr>";

        sb.append(
                "fillTable : function(tableToFill, xml){\n").append(
                "    tableToFill.innerHTML = \"").append(titleRowHTML).append("\";\n").append(
                "    $(xml).find(\"").append(entityTagName).append("\").each(function () {\n" +
                        "         var currEntity = $(this);\n").append(
                        "         var currEntityId = currEntity.attr(\"id\");\n").append(
                        "         var row = tableToFill.insertRow(tableToFill.rows.length);\n").append(
                        "         row.innerHTML = \"<td><input type=\\\"checkbox\\\" /><input type=\\\"button\\\" value=\\\"Delete\\\" onClick=\\\"").append(libraryName).append(".deleteEntity('\" + currEntityId + \"')\\\"/></td>\";\n").append(
                        "         var cell;\n");


        for (AbstractFieldMetadata currField : currEntity.getFields()){
            sb.append(
                "         cell = row.insertCell(row.cells.length);\n").append(
                "         cell.innerHTML = currEntity.find(\"").append(currField.getName()).append("\").text();\n");
        }

        for (AbstractRelation currRelationship : currEntity.getRelations()){
            sb.append(

                "         cell = row.insertCell(row.cells.length);\n");

            String relatedEntityTagName = XMLConvertersGenerator.generateRootTagName(store.getEntityMetadataByName(currRelationship.getRelatedEntity()));
            switch (currRelationship.getRelationType()){
                case OneToMany:
                case OneToOwner:
                    sb.append(
                        "         cell.innerHTML = currEntity.find(\"").append(relatedEntityTagName).append("\").text();\n");

                    break;
                case ManyToMany:
                    sb.append(
                            "         cell.innerHTML = \"\";\n").append(
                            "         currEntity.find(\"").append(EntityXMLConverter.RELATIONSHIP_ELEMENT_TAG_NAME).append("[name=").append(currRelationship.getRelationshipName()).append("]\").find(\"").append(relatedEntityTagName).append("\").each(function() {\n").append(
                            "             var currRelatedEntity = $(this);\n").append(
                            "             cell.innerHTML += currRelatedEntity.text() + \"<br/>\";\n").append(
                            "         });\n");


                break;
                default:
                    //todo:do
                    sb.append(
                            "         cell.innerHTML = \"").append(currRelationship.getRelatedEntity()).append("\";\n");

                    break;
            }
        }


        sb.append(
                "    });\n").append(
                "    var newEntityRow = tableToFill.insertRow(tableToFill.rows.length);\n").append(
                "    var newEntityCell = newEntityRow.insertCell(newEntityRow.cells.length);\n").append(
                "    newEntityCell.innerHTML = \"<input type=\\\"button\\\" value=\\\"Create\\\" onClick=\\\"").append(libraryName).append(".createNewEntity();\\\">\";\n");

        for (AbstractFieldMetadata currField : currEntity.getFields()){
            sb.append(
                "    newEntityCell = newEntityRow.insertCell(newEntityRow.cells.length);\n").append(
                "    newEntityCell.innerHTML = \"");

            switch (currField.getType()){
                case ENUM:
                    EnumFieldDef enumField = (EnumFieldDef)currField;
                    sb.append("<select id=\\\"fldNewEntity_").append(currField.getName()).append("\\\">");
                    for (GeneratedEnumValue currEnumEntry : pojoGenerator.getGeneratedEnumByField(enumField).getValues()){
                        sb.append("<option value=\\\"").append(currEnumEntry.getName()).append("\\\">").append(currEnumEntry.getName()).append("</option>");
                    }

                    sb.append("</select>");
                    break;
                case DATE:
                    //html5 date type. some browsers support that... some will display as text...
                    sb.append("<input type=\\\"date\\\" id=\\\"fldNewEntity_").append(currField.getName()).append("\\\"/>");
                    break;
                case BOOLEAN:
                    sb.append("<input type=\\\"checkbox\\\" id=\\\"fldNewEntity_").append(currField.getName()).append("\\\"/>");
                    break;
                default:
                    sb.append("<input type=\\\"text\\\" id=\\\"fldNewEntity_").append(currField.getName()).append("\\\"/>");
                    break;


            }

            sb.append("\";\n");

        }

        for (AbstractRelation currRelationship : currEntity.getRelations()){
            String relatedEntityLibraryName = generateLibraryName(store.getEntityMetadataByName(currRelationship.getRelatedEntity()));
            String relationshipCellVarName =  "cellRelationship" + StringUtil.capitalize(currRelationship.getRelationshipName());

            sb.append(
                    "    var ").append(relationshipCellVarName).append(" = newEntityRow.insertCell(newEntityRow.cells.length);\n");
            switch (currRelationship.getRelationType()){
                case OneToMany:
                case OneToOwner:
                    //will create select html element with id [generateNewEntityRelationshipFieldName]
                    sb.append("    ").append(relationshipCellVarName).append(".innerHTML = \"loading...\";\n").append(
                              "    ").append(relatedEntityLibraryName).append(".list( function(xml) {\n").append(
                              "        ").append(relationshipCellVarName).append(".innerHTML = ").append(relatedEntityLibraryName).
                                          append(".createEntityHTMLSelectFunction(xml, \"").
                                          append(generateNewEntityRelationshipFieldName(currRelationship.getRelationshipName())).
                                          append("\", ").append(Boolean.toString(!currRelationship.isMandatory())).append(");\n").append(
                            "    },\n").append(
                            "    function(errString) {\n").append(
                            "        ").append(relationshipCellVarName).append(".innerHTML = errString;\n").append(
                              "    });\n");


                    break;
                case ManyToMany:
                    //will create select html elements with ids [generateNewEntityRelationshipFieldName]_[sequence]
                    String addFunctionName = "add" + StringUtil.capitalize(currRelationship.getRelationshipName()) + "RowFunction";
                    String addButtonVarName = "btnAdd" + StringUtil.capitalize(currRelationship.getRelationshipName());
                    String relSpanVarName = "span" + StringUtil.capitalize(currRelationship.getRelationshipName());

                    sb.append("\n").append(
                            "    // Span will display loading until data arrives from server\n").append(
                            "    var ").append(relSpanVarName).append(" = document.createElement(\"span\");\n").append(
                            "    ").append(relSpanVarName).append(".innerHtml = \"loading...\";\n\n").append(

                            "    // Function for adding a select box for more than one related entity (many-to-many)\n").append(
                            "    var ").append(addFunctionName).append(" = function() {\n").append(
                            "        var idx = 1;\n").append(
                            "        var currSelectElement = document.getElementById(\"").append(generateNewEntityRelationshipFieldName(currRelationship.getRelationshipName())).append("_\" + idx);\n").append(
                            "\n").append(
                            "        // Finding the select element with the highest index and adding a new one\n").append(
                            "        while (currSelectElement != null){\n").append(
                            "            idx++;\n").append(
                            "            currSelectElement = document.getElementById(\"").append(generateNewEntityRelationshipFieldName(currRelationship.getRelationshipName())).append("_\" + idx);\n").append(
                            "        }\n").append(
                            "\n").append(
                            "        // Append another select row\n").append(
                            "        ").append(relatedEntityLibraryName).append(".list( function(xml) {\n").append(
                            "            ").append(relSpanVarName).append(".insertAdjacentHTML(\"beforeend\", \"<br/>\" + ").append(relatedEntityLibraryName).append(".createEntityHTMLSelectFunction(xml, \"").append(generateNewEntityRelationshipFieldName(currRelationship.getRelationshipName())).append("_\" + idx, false));\n").append(
                            "        },\n").append(
                            "        function(errString) {\n").append(
                            "            ").append(relationshipCellVarName).append(".innerHTML = errString;\n").append(
                            "        });\n").append(
                            "    };\n\n").append(

                            "    // Add button will add more select elements.\n").append(
                            "    var ").append(addButtonVarName).append(" = document.createElement(\"input\");\n").append(
                            "    ").append(addButtonVarName).append(".type = \"button\";\n").append(
                            "    ").append(addButtonVarName).append(".value = \"Add\"\n").append(
                            "    ").append(addButtonVarName).append(".onclick = ").append(addFunctionName).append(";\n\n").append(
                            "    // Adding both add button and span to the cell\n").append(
                            "    ").append(relationshipCellVarName).append(".appendChild(").append(addButtonVarName).append(");\n").append(
                            "    ").append(relationshipCellVarName).append(".appendChild(").append(relSpanVarName).append(");\n");


                    sb.append("    ").append(relatedEntityLibraryName).append(".list( function(xml) {\n").append(
                            "        ").append(relSpanVarName).append(".innerHTML = \"<br/>\" + ").append(relatedEntityLibraryName).append(".createEntityHTMLSelectFunction(xml, \"").append(
                                   generateNewEntityRelationshipFieldName(currRelationship.getRelationshipName())).append("_1\", ").append(
                                   Boolean.toString(!currRelationship.isMandatory())).append(");\n").append(
                            "    },\n").append(
                            "    function(errString) {\n").append(
                            "        ").append(relationshipCellVarName).append(".innerHTML = errString;\n").append(
                            "    });\n");
                    break;
                case OneToOne:
                    //todo:do
                    //sb.append("huh?");
                    break;
            }

        }

        sb.append( "}");




    }

    private String generateNewEntityRelationshipFieldName(String relationshipName){
        return "fldNewEntityRelationship_" + relationshipName;
    }

    private String generateLibraryName(DataEntityMetadata currEntity) {
        return StringUtil.capitalize(currEntity.getName()) + "Library";
    }

    private String generateJSFileName(DataEntityMetadata currEntity) {
        return currEntity.getName() + "Library.js";
    }

    private void generateCreateNewEntityFunction(DataEntityMetadata currEntity, StringBuilder sb, String libraryName) throws MetadataGeneratorException {
        String entityXMLElementName = XMLConvertersGenerator.generateRootTagName(currEntity);
        String entityResourcePath = RestLayerGenerator.getResourcePath(currEntity);


        sb.append("createNewEntity : function(){\n").append(
                  "    log(\"Creating new entity\");\n");
        sb.append("    var eventXml = \"<").append(entityXMLElementName).append(">");
        for (AbstractFieldMetadata currField : currEntity.getFields()){
            sb.append("<").append(currField.getName()).append(">\" + fldNewEntity_").append(currField.getName()).append(".value + \"</").append(currField.getName()).append(">");
        }
        sb.append("\";\n");

        if (!currEntity.getRelations().isEmpty()){
            sb.append("    eventXml += \"<").append(EntityXMLConverter.RELATIONSHIPS_ELEMENT_TAG_NAME).append(">\";\n");
            for (AbstractRelation currRelationship : currEntity.getRelations()){
                String relatedEntityName = currRelationship.getRelatedEntity();
                DataEntityMetadata relatedEntityMetadata = store.getEntityMetadataByName(relatedEntityName);
                String relationshipName = currRelationship.getRelationshipName();
                String relationshipEntryElementTagName = currRelationship.getRelationshipName();
                String relationshipEntryWrapperElementTagName = StringUtil.getPluralForm(currRelationship.getRelationshipName());
                String newEntityRelationshipHtmlFieldName = generateNewEntityRelationshipFieldName(relationshipName);
                String targetEntityIdFieldName = PojoGenerator.generateEntityIdFieldName(relatedEntityMetadata);

                switch (currRelationship.getRelationType()){
                    case OneToMany:
                    case OneToOwner:
                        sb.append(
                                "    if (").append(newEntityRelationshipHtmlFieldName).append(".value != \"null\"){\n").append(
                                "        eventXml += \"<").append(EntityXMLConverter.RELATIONSHIP_ELEMENT_TAG_NAME).append(" name=\\\"").append(relationshipName).append("\\\">\";\n").append(
                                "        eventXml += \"<").append(relationshipEntryElementTagName).append("><").append(targetEntityIdFieldName).append(">\" + ").append(newEntityRelationshipHtmlFieldName).append(".value + \"</" ).append (targetEntityIdFieldName).append("></").append(relationshipEntryElementTagName).append(">\";\n").append(
                                "        eventXml += \"</").append(EntityXMLConverter.RELATIONSHIP_ELEMENT_TAG_NAME).append(">\";\n").append(
                                "    }\n");
                        break;
                    case ManyToMany:
                        String wrapperElementTagName = XMLConvertersGenerator.generateWrapperRootTagName(relatedEntityMetadata);
                        sb.append(
                            "    if (").append(newEntityRelationshipHtmlFieldName).append("_1.value != \"null\"){\n").append(
                            "        eventXml += \"<").append(EntityXMLConverter.RELATIONSHIP_ELEMENT_TAG_NAME).append(" name=\\\"").append(relationshipName).append("\\\">\";\n").append(
                            "        eventXml += \"<").append(relationshipEntryWrapperElementTagName).append(">\";\n").append(
                            "        var currRelatedEntitySelectElement = document.getElementById(\"").append(newEntityRelationshipHtmlFieldName).append("_1\");\n").append(
                            "        var idx = 1;\n").append(
                            "        while (currRelatedEntitySelectElement != null) {\n").append(
                            "            if (currRelatedEntitySelectElement.value != \"null\"){\n").append(
                            "                eventXml += \"<").append(relationshipEntryElementTagName).append("><").append(targetEntityIdFieldName).append(">\" + currRelatedEntitySelectElement.value + \"</" ).append (targetEntityIdFieldName).append("></").append(relationshipEntryElementTagName).append(">\";\n").append(
                            "            }\n").append(
                            "            ++idx;\n").append(
                            "            currRelatedEntitySelectElement = document.getElementById(\"").append(newEntityRelationshipHtmlFieldName).append("_\" + idx);\n").append(
                            "        }\n").append(
                            "        eventXml += \"</").append(relationshipEntryWrapperElementTagName).append(">\";\n").append(
                            "        eventXml += \"</").append(EntityXMLConverter.RELATIONSHIP_ELEMENT_TAG_NAME).append(">\";\n").append(
                            "    }\n");
                        break;
                    default:
                        //todo do
                        break;
                }

            }
            sb.append("    eventXml += \"</").append(EntityXMLConverter.RELATIONSHIPS_ELEMENT_TAG_NAME).append(">\";\n");
        }
        sb.append("    eventXml += \"</").append(entityXMLElementName).append(">\";\n");
        sb.append(
                  "    new Ajax.Request(\"../rest/").append(entityResourcePath).append("\", {\n").append(
                  "        method: 'post',\n").append(
                  "        evalJS: false,\n").append(
                  "        contentType: 'application/xml',\n").append(
                  "        postBody: eventXml,\n").append(
                  "        onSuccess: function(transport) {\n").append(
                  "            log(\"created successfully\");\n").append(
                  "            getAll();\n").append(
                  "        },\n").append(
                  "        onFailure: function(transport) {\n").append(
                  "            log(\"Failed creating. server returned \" + transport.status + \": \" + transport.statusText);\n").append(
                  "            alert(\"Failed creating. server returned \" + transport.status + \": \" + transport.statusText);\n").append(
                  "        },\n").append(
                  "        onException: function(request, e) {\n").append(
                  "            log(\"Exception occurred \" + e);\n").append(
                  "            alert(\"Exception occurred \" + e);\n").append(
                  "        }\n").append(
                  "    });\n").append(
                  "\n").append(
                "}");
    }

    private void generateDeleteEntityFunction(DataEntityMetadata currEntity, StringBuilder sb, String libraryName) throws MetadataGeneratorException {
        String entityResourcePath = RestLayerGenerator.getResourcePath(currEntity);
        sb.append("deleteEntity : function(entityId) {\n").append(
                  "    log(\"Deleting entity of type ").append(currEntity.getName()).append(" with id \" + entityId);\n").append(
                "    var request =\n").append(
                        "        $.ajax({\n").append(
                        "            url:\"../rest/").append(entityResourcePath).append("\",\n").append(
                        "            type:\"DELETE\",\n").append(
                        "            data: entityId\n").append(
                        "            });\n").append(
                        "\n").append(
                        "    request.done(function(transport) {\n").append(
                        "        log(\"deleted successfully\");\n").append(
                        "        getAll();\n").append(
                        "    });\n").append(
                        "    request.fail(function(jqXHR, textStatus) {\n").append(
                        "        log(\"Failed deleting. server returned \" + textStatus);\n").append(
                        "        alert(\"Failed deleting. server returned \" + textStatus);\n").append(
                        "    });\n").append(
                "}");
    }

    private void generateRootResourcePathMethod(DataEntityMetadata currEntity, GeneratedClass c, GeneratedClass currRestResource) {
        ArrayList<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();

        lines.add(new GeneratedCodeLine("return " + currRestResource.getName() + "." + RestLayerGenerator.RESOURCE_ROOT_PATH_FIELD_NAME + ";", Arrays.asList(new ClassGeneratedType(currRestResource))));
        c.addMethod(new GeneratedMethod("getRootResourcePath", GeneratedModifier.PROTECTED, Collections.<GeneratedParameter>emptyList(), new GeneratedType("String"), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(lines)));
    }

    private void generateGetClassTypeMethod(GeneratedClass currPojo, GeneratedClass c) throws MetadataGeneratorException {
        ArrayList<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
        lines.add(new GeneratedCodeLine("return " + currPojo.getName() + ".class;", Arrays.asList(new ClassGeneratedType(currPojo))));
        c.addMethod(new GeneratedMethod("getClassType", GeneratedModifier.PROTECTED, Collections.<GeneratedParameter>emptyList(), new GeneratedType("Class<" + currPojo.getName() + ">"), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(lines)));
    }


    @Override
    public Set<Class<? extends IMetadataGenerator>> getDependencies() {
        Set<Class<? extends IMetadataGenerator>> dependencies = new HashSet<Class<? extends IMetadataGenerator>>();
        dependencies.add(PojoGenerator.class);
        return dependencies;
    }

    @Override
    public void setProperties(Map<String, String> properties) throws MetadataGeneratorException {
    }

    @Override
    public void write(String rootDir) throws MetadataGeneratorException {
        super.write(rootDir);

        for (Map.Entry<String, StringBuilder> currEntry : generatedJSResources.entrySet()) {
            String jsFileName = currEntry.getKey();
            File jsClassFile = new File(rootDir + "/target/generated-resources/shpangen/webmanage/" + jsFileName);
            logger.info("Generating js {} to {}", jsFileName, jsClassFile.getAbsolutePath());
            try {
                //todo: yeah yeah, I know toString is not good here...
                FileUtils.writeStringToFile(jsClassFile, currEntry.getValue().toString());
            } catch (IOException e) {
                throw new MetadataGeneratorException("Failed generating js resource " + jsFileName, e);
            }
        }


    }
}
