package com.demoproject.demo.pacmedproductivity;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for querying user productivity metrics.
 * This class encapsulates key productivity indicators for a user.
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProductivityQueryDTO {
    private String username;
    private Long totalSubmissions;
    private Long totalPouchesChecked;
    private Long totalMinutes;
}