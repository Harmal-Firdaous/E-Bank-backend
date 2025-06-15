package ebanking_backend.ebanking_backend.dtos;

import lombok.Data;

@Data
public class CustomerDto {
    private Long id;
    private String name;
    private String email;
}