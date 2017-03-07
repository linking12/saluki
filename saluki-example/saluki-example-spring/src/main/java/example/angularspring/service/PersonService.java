package example.angularspring.service;

import java.util.List;

import example.angularspring.dto.Person;

/**
 * Service to handle Persons.
 */
public interface PersonService {
    List<Person> getAllPersons();

    void addPerson(Person person);

    void deletePerson(int id);
}
