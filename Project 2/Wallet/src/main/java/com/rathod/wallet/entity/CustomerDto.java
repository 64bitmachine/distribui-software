
package com.rathod.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rathod
 *         custumer id, balance
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {

    // custumer id
    private int custId;
    // balance
    private double balance;
}