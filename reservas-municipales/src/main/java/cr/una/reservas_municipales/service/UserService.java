package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.UserDto;
import cr.una.reservas_municipales.model.User;
import cr.una.reservas_municipales.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserDto> listAll() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<UserDto> getById(UUID id) {
        return userRepository.findById(id).map(this::toDto);
    }

    private UserDto toDto(User u) {
        UserDto d = new UserDto();
        d.setUserId(u.getUserId());
        d.setEmail(u.getEmail());
        d.setFullName(u.getFullName());
        d.setPhone(u.getPhone());
        d.setActive(u.isActive());
        if (u.getRole() != null) d.setRoleCode(u.getRole().getCode());
        return d;
    }
}

