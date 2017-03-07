package example.angularspring.web;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import example.angularspring.dto.Person;
import example.angularspring.dto.ResponseMessage;
import example.angularspring.service.PersonService;

/**
 * Controller for person actions.
 */
@Controller
public class PersonController {
    @Inject
    private PersonService personService;

    @RequestMapping(value = "/person", method = RequestMethod.GET)
    @ResponseBody
    public List<Person> getPersons() {
        return personService.getAllPersons();
    }

    @RequestMapping(value = "/person", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMessage addPerson(@RequestBody Person person) {
        if (person.getFirstName().length() <= 3 || person.getLastName().length() <= 3) {
            throw new IllegalArgumentException("moreThan3Chars");
        }
        personService.addPerson(person);
        return new ResponseMessage(ResponseMessage.Type.success, "personAdded");
    }

    @RequestMapping(value = "/person/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseMessage deletePerson(@PathVariable int id) {
        personService.deletePerson(id);
        return new ResponseMessage(ResponseMessage.Type.success, "personDeleted");
    }

}
