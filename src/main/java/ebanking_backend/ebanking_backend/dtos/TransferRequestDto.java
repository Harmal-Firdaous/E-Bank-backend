package ebanking_backend.ebanking_backend.dtos;


import lombok.Data;

@Data
public class TransferRequestDto {
    private String accountSource;
    private String accountDestination;
    private double amount;
    private String description;
}
