package com.shpandrak.metadata.model;

import com.shpandrak.metadata.model.field.AbstractFieldMetadata;
import com.shpandrak.metadata.model.field.StringFieldDef;
import com.shpandrak.metadata.model.relation.AbstractRelation;
import com.shpandrak.metadata.model.relation.OneToManyRelation;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/10/12
 * Time: 22:02
 */
public class TestMetadataStoreSerialization {

    @Test
    public void testSerialization() throws Exception {
        DataEntityMetadata entity =
                new DataEntityMetadata(
                        "person",
                        null,
                        null,
                        Arrays.<AbstractFieldMetadata>asList(
                            new StringFieldDef("name", 255),
                            new StringFieldDef("description", 255)),
                        Arrays.<AbstractRelation>asList(new OneToManyRelation("country", true, "country", "countryId")));

        MetadataStore store = new MetadataStore("com.visilaw", Arrays.asList(entity));
        JAXBContext jaxbContext = null;
        jaxbContext = JAXBContext.newInstance(MetadataStore.class);
        Marshaller jaxbMarshaller = jaxbContext. createMarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter stringWriter = new StringWriter();
        jaxbMarshaller.marshal(store, stringWriter);
        stringWriter.flush();

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        MetadataStore store2 = (MetadataStore) unmarshaller.unmarshal(new StringReader(stringWriter.getBuffer().toString()));
        StringWriter stringWriter1 = new StringWriter();
        jaxbMarshaller.marshal(store2, stringWriter1);
        System.out.println(stringWriter.toString());
        System.out.println(stringWriter1.toString());
        Assert.assertEquals("Strings not equal", stringWriter.toString(), stringWriter1.toString());

    }
}
