package com.example.DATN.service;

import com.example.DATN.dto.role.RoleRequest;
import com.example.DATN.dto.role.RoleResponse;
import com.example.DATN.entity.Role;
import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;

    public RoleResponse createRole(RoleRequest request){
       if(roleRepository.existsByName(request.getName())){
           throw new AppException(ErrorCode.ROLE_EXISTED);
       }
        Role role=new Role();
       role.setName(request.getName());
       roleRepository.save(role);
       return toRoleResponse(role);
    }

    public List<RoleResponse> getAllRole(){
        return roleRepository.findAll().stream().map(this::toRoleResponse).toList();
    }

    public void deleteRole(Long idRole){
        roleRepository.deleteById(idRole);
    }


    public RoleResponse toRoleResponse(Role role){
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}
