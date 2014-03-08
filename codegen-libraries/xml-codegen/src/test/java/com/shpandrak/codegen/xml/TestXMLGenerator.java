package com.shpandrak.codegen.xml;

import com.shpandrak.codegen.PojoGenerator;
import com.shpandrak.metadata.generator.GenerationContext;
import com.shpandrak.metadata.generator.IMetadataGenerator;
import com.shpandrak.metadata.model.*;
import com.shpandrak.metadata.model.field.AbstractFieldMetadata;
import com.shpandrak.metadata.model.field.StringFieldDef;
import com.shpandrak.metadata.model.relation.AbstractRelation;
import com.shpandrak.metadata.model.relation.OneToManyRelation;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/16/12
 * Time: 08:35
 */
public class TestXMLGenerator {
    @Test
    public void testXMLConverterGenerator() throws Exception {
        DataEntityMetadata country = new DataEntityMetadata("country",
                null,
                null,
                Arrays.<AbstractFieldMetadata>asList(
                        new StringFieldDef("name", 255),
                        new StringFieldDef("description", 255)),
                Collections.<AbstractRelation>emptyList());

        DataEntityMetadata person = new DataEntityMetadata("person",
                null,
                null,
                Arrays.<AbstractFieldMetadata>asList(
                        new StringFieldDef("name", 255),
                        new StringFieldDef("description", 255)),
                Arrays.<AbstractRelation>asList(new OneToManyRelation("country", true, "country", "countryId")));


        MetadataStore store = new MetadataStore("com.shpandrak.test", Arrays.asList(country, person));
        PojoGenerator pojoGenerator = new PojoGenerator();
        GenerationContext generationContext = new GenerationContext();
        pojoGenerator.generate(store, generationContext);
        generationContext.addGenerator(pojoGenerator);

        new XMLConvertersGenerator().generate(store, generationContext);
    }

}
