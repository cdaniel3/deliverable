package tech.corydaniel.web;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import tech.corydaniel.exceptions.PriorityNotFoundException;
import tech.corydaniel.model.Priority;
import tech.corydaniel.repositories.PriorityRepository;

@RestController
@RequestMapping("/priorities")
public class PriorityRESTController {

	@Autowired
	private PriorityRepository priorityRepository;
	
	@RequestMapping(method=RequestMethod.GET)
	public List<Priority> getPriorities() {
		return getPriorityRepository().findAll();
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/{priorityId}")
	public Priority getPriority(@PathVariable Long priorityId) {
		Optional<Priority> optionalPriority = getPriorityRepository().findById(priorityId);
		if (optionalPriority.isEmpty()) {
			throw new PriorityNotFoundException("Priority not found. Id: " + priorityId);
		}
		return optionalPriority.get();
	}

	public PriorityRepository getPriorityRepository() {
		return priorityRepository;
	}
}
