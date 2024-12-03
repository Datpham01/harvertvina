package vn.fs.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

import vn.fs.entities.Category;
import vn.fs.entities.Origins;
import vn.fs.entities.User;
import vn.fs.repository.CategoryRepository;
import vn.fs.repository.OriginsRepository;
import vn.fs.repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class OriginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OriginsRepository originsRepository;

    @ModelAttribute(value = "user")
    public User user(Model model, Principal principal, User user) {

        if (principal != null) {
            model.addAttribute("user", new User());
            user = userRepository.findByEmail(principal.getName());
            model.addAttribute("user", user);
        }

        return user;
    }

    // show list origin - table list
    @ModelAttribute("origins")
    public List<Origins> showCategory(Model model) {
        List<Origins> origins = originsRepository.findAll();
        model.addAttribute("origins", origins);

        return origins;
    }

    // add origin
    @PostMapping(value = "/addOrigin")
    public String addOrigin(@Validated @ModelAttribute("originAdd") Origins originAdd, ModelMap model,
                              BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "failure");

            return "admin/origins";
        }

        originsRepository.save(originAdd);
        model.addAttribute("message", "successful!");

        return "redirect:/admin/origins";
    }

    // get Edit origin
    @GetMapping(value = "/editOrigin/{id}")
    public String editCategory(@PathVariable("id") Long id, ModelMap model) {
        Origins originAdd = originsRepository.findById(id).orElse(null);
        model.addAttribute("originAdd", originAdd);

        return "admin/editOrigins";
    }

    @GetMapping("/origins/delete/{id}")
    public String delCategory(@PathVariable("id") Long id, Model model) {
        originsRepository.deleteById(id);

        model.addAttribute("message", "Delete successful!");

        return "redirect:/admin/origins";
    }

    @GetMapping("/origins")
    public String getOrigin(Model model, Principal principal){
        model.addAttribute("originAdd", new Origins());
        return "admin/origins";
    }
}
