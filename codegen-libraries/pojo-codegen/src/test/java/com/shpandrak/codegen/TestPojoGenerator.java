package com.shpandrak.codegen;

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
 * Date: 10/12/12
 * Time: 00:15
 */
public class TestPojoGenerator {
    @Test
    public void testPojoGenerator() throws Exception {

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


        MetadataStore store = new MetadataStore("com.visilaw", Arrays.asList(person, country));
        new PojoGenerator().generate(store, new GenerationContext());

    }
}
