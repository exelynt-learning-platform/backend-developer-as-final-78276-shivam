package com.example.resourcebooking.service;

import com.example.resourcebooking.dto.LoginRequestDto;
import com.example.resourcebooking.dto.LoginResponseDto;



public interface AuthService {
	
 LoginResponseDto login(LoginRequestDto request);

}
