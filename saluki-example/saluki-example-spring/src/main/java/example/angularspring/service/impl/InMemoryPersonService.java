package example.angularspring.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import example.angularspring.dto.Person;
import example.angularspring.service.PersonService;

/**
 * Simple Map based PersonService.
 */
@Service
public class InMemoryPersonService implements PersonService {
    private Map<Integer, Person> persons = new HashMap<Integer, Person>();

    public InMemoryPersonService() {
        persons.put(1, new Person(1, "Lionel", "Messi"));
        persons.put(2, new Person(2, "Cristiano", "Ronaldo"));
    }

    public List<Person> getAllPersons() {
        return new ArrayList<Person>(persons.values());
    }

    public void addPerson(Person person) {
        int id = 1;
        while (persons.get(id) != null) {
            id++;
        }
        person.setId(id);
        persons.put(id, person);
    }

    public void deletePerson(int id) {
        persons.remove(id);
    }
}
