package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.role.RoleRequest;
import com.example.DATN.dto.role.RoleResponse;
import com.example.DATN.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequestMapping("/role")
public class RoleController {
    RoleService roleService;

    @PostMapping()
    public ApiResponse<RoleResponse> createRole(@RequestBody RoleRequest request){
        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setResult(roleService.createRole(request));
        return apiResponse;
    }

    @GetMapping()
    public ApiResponse<List<RoleResponse>> getAllRole(){
        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setResult(roleService.getAllRole());
        return apiResponse;
    }

    @DeleteMapping("/{roleId}")
    public ApiResponse<String> deleteRole(@RequestParam Long roleId){
        ApiResponse apiResponse=new ApiResponse();
        roleService.deleteRole(roleId);
        apiResponse.setMessage("Delete Success");
        return apiResponse;
    }
}
