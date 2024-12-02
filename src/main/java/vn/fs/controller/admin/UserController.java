package vn.fs.controller.admin;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import vn.fs.dto.UserUpdate;
import vn.fs.entities.Category;
import vn.fs.entities.Product;
import vn.fs.entities.Role;
import vn.fs.entities.User;
import vn.fs.repository.RoleRepository;
import vn.fs.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;


@Controller
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping(value = "/admin/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String customer(Model model, Principal principal) {

        User user = userRepository.findByEmail(principal.getName());
        model.addAttribute("user", user);

        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);

        return "/admin/users";
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/admin/users/{userId}/lock")
    public String lockUser(@PathVariable("userId") Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setStatus(false); // Khóa tài khoản
            userRepository.save(user);
        }
        return "redirect:/admin/users";
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/admin/users/{userId}/unlock")
    public String unlockUser(@PathVariable("userId") Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setStatus(true); // Mở khóa tài khoản
            userRepository.save(user);
        }
        return "redirect:/admin/users";
    }

    @ModelAttribute(value = "user")
    public User user(Model model, Principal principal, User user) {

        if (principal != null) {
            model.addAttribute("user", new User());
            user = userRepository.findByEmail(principal.getName());
            model.addAttribute("user", user);
        }

        return user;
    }

    @GetMapping(value = "/admin/editUser/{id}")
    public String editUser(@PathVariable("id") Long id, ModelMap model) {
        User user = userRepository.findById(id).orElse(null);

        UserUpdate userUpdate = createUserUpdate(user);

        List<Role> roles = roleRepository.findAll();

        model.addAttribute("roles", roles);
        model.addAttribute("userUpdate", userUpdate);

        return "admin/editUser";
    }

    @PostMapping("/admin/update-user")
    public String updateUser(@ModelAttribute("userUpdate") UserUpdate userUpdate, Model model, HttpServletRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentEmail = userDetails.getUsername();

        User user = userRepository.findById(userUpdate.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findById(userUpdate.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setName(userUpdate.getName());

        Set<Role> newRoles = new HashSet<>();
        newRoles.add(role);
        user.setRoles(newRoles);

        User user1 = userRepository.save(user);

        User currentUser = userRepository.findByEmail(currentEmail);

        if (user1 != null) {
            model.addAttribute("message", "Update success");
            // Kiểm tra nếu userId trùng với userId đang đăng nhập
            if (currentUser.getUserId().equals(userUpdate.getUserId())) {
                // Logout
                SecurityContextHolder.clearContext();
                request.getSession().invalidate();
                // Chuyển hướng đến trang login
                return "redirect:/login";
            }
        } else {
            model.addAttribute("message", "Update failure");
        }
        return "redirect:/admin/users";
    }

    private UserUpdate createUserUpdate(User user) {
        Role firstRole = user.getRoles().iterator().next();
        Long firstRoleId = firstRole.getId();

        return UserUpdate.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .roleId(firstRoleId)
                .build();
    }
}
