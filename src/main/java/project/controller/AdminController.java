package project.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.dto.UserResponseDto;
import project.service.UserService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;

    //api/admin/users?page=0&size=10&sort=name,asc
    //1
    @GetMapping("/users")
    public Page<UserResponseDto> getUsers(
            @PageableDefault(size = 5, sort = "id") Pageable pageable
    ) {
        return userService.getUsers(pageable);
    }

    //2
    /*@GetMapping("/users")
    public Page<UserResponseDto> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return userService.getUsers(page, size);*/
    }
}

