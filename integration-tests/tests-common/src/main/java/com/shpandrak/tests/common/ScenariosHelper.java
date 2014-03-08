package com.shpandrak.tests.common;

import com.shpandrak.datamodel.OrderByClauseEntry;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.PersistenceLayerManager;
import com.shpandrak.persistence.managers.IEntityManager;
import com.shpandrak.persistence.managers.ManagerClassFactory;
import com.shpandrak.persistence.query.filter.BasicFieldFilterCondition;
import com.shpandrak.persistence.query.filter.FilterConditionOperatorType;
import com.shpandrak.persistence.query.filter.QueryFilter;
import com.shpandrak.world.model.Country;
import com.shpandrak.world.model.Gender;
import com.shpandrak.world.model.Person;
import com.shpandrak.world.model.PersonVisitCountryRelationshipEntry;
import org.junit.Assert;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/27/13
 * Time: 16:54
 */
public class ScenariosHelper {

    public static void runTestCode() throws PersistenceException {

        PersistenceLayerManager.beginOrJoinConnectionSession();
        try {

            IEntityManager<Person> personManager = (IEntityManager<Person>) ManagerClassFactory.getDefaultInstance(Person.class);
            IEntityManager<Country> countryManager = (IEntityManager<Country>) ManagerClassFactory.getDefaultInstance(Country.class);
            Country armenia = new Country("Armenia");
            Country italy = new Country("Italy");
            countryManager.create(Arrays.asList(armenia, italy));

            Person armeni = new Person("Armeni", "Armani", Gender.M, 170, new Date(), armenia);
            armeni.getVisitCountryRelationship().addNewRelation(new PersonVisitCountryRelationshipEntry(armenia));
            armeni.getVisitCountryRelationship().addNewRelation(new PersonVisitCountryRelationshipEntry(italy));
            personManager.create(armeni);

            Person francesco = new Person("Francesco", "Ravaneli", Gender.M, 168, new Date(), italy);
            francesco.setFatherId(armeni.getFatherId());

            francesco.getVisitCountryRelationship().addNewRelation(new PersonVisitCountryRelationshipEntry(italy));
            personManager.create(francesco);

            List<Person> persons = personManager.list();
            Map<Key, Person> personMapById = personManager.getMapById();
            Assert.assertEquals("Wrong number of persons", 2, personMapById.size());
            Assert.assertEquals("Wrong number of persons", 2, persons.size());
            persons = personManager.listByField(Person.DESCRIPTOR.lastNameFieldDescriptor, "Armani");
            Assert.assertEquals("Wrong number of persons", 1, persons.size());
            persons = personManager.listByField(Person.DESCRIPTOR.firstNameFieldDescriptor, "Armani");
            Assert.assertEquals("Wrong number of persons", 0, persons.size());
            persons = personManager.listByRelationShip(Person.DESCRIPTOR.birthCountryRelationshipDescriptor, armenia.getId());
            Assert.assertEquals("Wrong number of persons", 1, persons.size());
            persons = personManager.listByRelationShip(Person.DESCRIPTOR.birthCountryRelationshipDescriptor, italy.getId());
            Assert.assertEquals("Wrong number of persons", 1, persons.size());
            Assert.assertEquals("Wrong person", francesco.getId(), persons.get(0).getId());
            persons = personManager.listByRelationShip(Person.DESCRIPTOR.visitCountryRelationshipDescriptor, armenia.getId());
            Assert.assertEquals("Wrong number of persons", 1, persons.size());
            persons = personManager.listByRelationShip(Person.DESCRIPTOR.visitCountryRelationshipDescriptor, italy.getId());
            Assert.assertEquals("Wrong number of persons", 2, persons.size());

            List<Country> countries = countryManager.list();
            Assert.assertEquals("Wrong number of countries", 2, countries.size());

            Map<Key, Country> countryMapById = countryManager.getMapById();
            Assert.assertEquals("Wrong number of countries", 2, countryMapById.size());
            countryMapById = countryManager.getMapById(new QueryFilter(BasicFieldFilterCondition.build(Country.DESCRIPTOR.nameFieldDescriptor, FilterConditionOperatorType.EQUALS, "Italy")));
            Assert.assertEquals("Wrong number of countries", 1, countryMapById.size());

            List<Country> orderedCountries = countryManager.list(new QueryFilter(null, null, null, Arrays.asList(new OrderByClauseEntry(Country.DESCRIPTOR.nameFieldDescriptor, true))));
            Assert.assertEquals("Invalid sort", "Armenia", orderedCountries.get(0).getName());
            Assert.assertEquals("Invalid sort", "Italy", orderedCountries.get(1).getName());

            orderedCountries = countryManager.list(new QueryFilter(null, null, null, Arrays.asList(new OrderByClauseEntry(Country.DESCRIPTOR.nameFieldDescriptor, false))));
            Assert.assertEquals("Invalid sort", "Italy", orderedCountries.get(0).getName());
            Assert.assertEquals("Invalid sort", "Armenia", orderedCountries.get(1).getName());

        } finally {
            PersistenceLayerManager.endJointConnectionSession();
        }
    }

}
