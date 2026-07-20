package com.monika.payflow.wallet.service;

import com.monika.payflow.user.entity.User;

public interface WalletProvisioningService {

    void createWalletForUser(User user);
}
