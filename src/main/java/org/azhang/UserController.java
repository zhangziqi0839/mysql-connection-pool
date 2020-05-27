package org.azhang;

import org.azhang.model.User;
import org.azhang.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;

@Controller
@RequestMapping(path="/demo")
public class UserController {
    @Inject
    private UserRepository userRepository;

    @PostMapping(path="/addUser")
    public @ResponseBody String addNewUser(
            @RequestParam String name,
            @RequestParam String email) {

        User user = new User(name, email);
        userRepository.save(user);
        return "Saved";
    }

    @GetMapping(path="/findAllUsers")
    public @ResponseBody List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping(path="/findUser")
    public @ResponseBody User getUserByUsername(String username) {
        // This returns a JSON or XML with the users
        return userRepository.findUserByUsername(username);
    }

}
