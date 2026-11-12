// National Philately Deposit Account Dashboard Logic
document.addEventListener('DOMContentLoaded', () => {
    if (!Auth.isLoggedIn()) {
        showToast('Please log in to access your National Philately Deposit Account', 'warning');
        setTimeout(() => location.href = 'login.html', 800);
        return;
    }

    loadDashboardSummary();
    loadWishlist();
    loadOrderHistory();
    loadUserProfile();
});

async function loadDashboardSummary() {
    try {
        const res = await API.get('/api/account/summary');
        if (res.success && res.data) {
            const d = res.data;
            const balEl = document.getElementById('summary-balance');
            const pendingEl = document.getElementById('summary-pending');
            const totalEl = document.getElementById('summary-total-items');
            const pdaNumEl = document.getElementById('summary-pda-number');

            if (balEl) balEl.textContent = `₹${parseFloat(d.balance).toFixed(2)}`;
            if (pendingEl) pendingEl.textContent = d.pendingOrders;
            if (totalEl) totalEl.textContent = d.totalOrders;
            if (pdaNumEl) pdaNumEl.textContent = d.accountNumber;

            const u = Auth.getUser();
            if (u) {
                u.balance = d.balance;
                u.accountNumber = d.accountNumber;
                localStorage.setItem('philately_user', JSON.stringify(u));
                renderHeaderAuth();
            }
        }
    } catch (e) {
        console.error('Failed to load account summary', e);
    }
}

async function loadWishlist() {
    const listEl = document.getElementById('wishlist-items-container');
    if (!listEl) return;

    try {
        const res = await API.get('/api/wishlist');
        if (res.success && res.data) {
            if (res.data.length === 0) {
                listEl.innerHTML = `<li style="padding: 20px; color: #64748b; text-align: center;">Your wishlist is currently empty.</li>`;
                return;
            }

            listEl.innerHTML = res.data.map(w => `
                <li>
                    <div class="wishlist-item">
                        <div style="display: flex; align-items: center; gap: 14px;">
                            <img src="${w.item.imageUrl || 'https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=100'}" style="width: 54px; height: 54px; object-fit: contain; background: #f8fafc; border-radius: 6px; border: 1px solid #e2e8f0; padding: 2px;">
                            <div>
                                <h4 style="margin: 0 0 4px 0; font-size: 0.95rem; color: var(--primary);">${w.item.title}</h4>
                                <span style="font-size: 0.9rem; color: var(--secondary); font-weight: 700;">₹${parseFloat(w.item.price).toFixed(2)}</span>
                            </div>
                        </div>
                        <div style="display: flex; gap: 8px;">
                            <button class="btn-primary" style="padding: 6px 14px; font-size: 0.85rem;" onclick="orderItemFromWishlist(${w.item.id}, ${w.id})">
                                <i class="fas fa-shopping-bag"></i> Order
                            </button>
                            <button class="remove-btn" onclick="removeWishlistItem(${w.item.id})" title="Remove item">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </li>
            `).join('');
        }
    } catch (e) {
        console.error('Failed to load wishlist', e);
    }
}

async function removeWishlistItem(itemId) {
    const res = await API.delete(`/api/wishlist/${itemId}`);
    if (res.success) {
        showToast('Removed from wishlist', 'info');
        loadWishlist();
    }
}

async function orderItemFromWishlist(itemId, wishlistId) {
    const res = await API.post('/api/orders', {
        paymentMethod: 'PDA_WALLET',
        items: [{ itemId: itemId, quantity: 1 }]
    });

    if (res.success && res.data) {
        showToast(`Order Placed! Consignment Tracking No: ${res.data.trackingNumber}`, 'success');
        await API.delete(`/api/wishlist/${itemId}`);
        loadDashboardSummary();
        loadWishlist();
        loadOrderHistory();
    } else {
        showToast(res.message || 'Order failed. Please check PDA balance.', 'error');
    }
}

async function loadOrderHistory() {
    const tbody = document.getElementById('order-history-tbody');
    const trackDetailsContainer = document.getElementById('tracking-details-container');
    if (!tbody) return;

    try {
        const res = await API.get('/api/orders');
        if (res.success && res.data) {
            const orders = res.data;
            if (orders.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #64748b; padding: 24px;">No order history found.</td></tr>`;
                if (trackDetailsContainer) {
                    trackDetailsContainer.innerHTML = `<p style="color: #64748b; padding: 10px;">No active consignments to track.</p>`;
                }
                return;
            }

            tbody.innerHTML = orders.map(o => `
                <tr>
                    <td><strong>#${o.orderNumber}</strong></td>
                    <td>${new Date(o.createdAt).toLocaleDateString()}</td>
                    <td><strong>₹${parseFloat(o.totalAmount).toFixed(2)}</strong></td>
                    <td><span class="badge badge-${o.orderStatus.toLowerCase()}">${o.orderStatus.replace('_', ' ')}</span></td>
                    <td>
                        <button class="btn-outline" style="padding: 5px 12px; font-size: 0.82rem;" onclick="viewOrderTracking('${o.orderNumber}')">
                            <i class="fas fa-truck"></i> Track Consignment
                        </button>
                    </td>
                </tr>
            `).join('');

            if (trackDetailsContainer) {
                trackDetailsContainer.innerHTML = orders.slice(0, 3).map(o => `
                    <div class="tracking-card" style="background: #f8fafc; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 16px; margin-bottom: 12px;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                            <strong style="color: var(--primary); font-size: 0.95rem;">Order #${o.orderNumber}</strong>
                            <span class="badge badge-${o.orderStatus.toLowerCase()}">${o.orderStatus.replace('_', ' ')}</span>
                        </div>
                        <p style="margin: 4px 0; font-size: 0.88rem;"><strong>Speed Post:</strong> <code>${o.trackingNumber || 'Assigned upon dispatch'}</code></p>
                        <p style="margin: 4px 0; font-size: 0.85rem; color: var(--text-muted);"><strong>Expected Delivery:</strong> ${o.expectedDeliveryDate || 'Within 5 business days'}</p>
                        <button class="btn-outline" style="margin-top: 8px; padding: 5px 12px; font-size: 0.82rem;" onclick="viewOrderTracking('${o.orderNumber}')">
                            <i class="fas fa-route"></i> Live Milestone Stepper
                        </button>
                    </div>
                `).join('');
            }
        }
    } catch (e) {
        console.error('Failed to load orders', e);
    }
}

// Deposit Top-Up Modal
function openTopUpModal() {
    document.getElementById('topup-modal').classList.add('active');
}

function closeTopUpModal() {
    document.getElementById('topup-modal').classList.remove('active');
}

function setTopUpAmount(amt) {
    document.getElementById('topup-amount-input').value = amt;
}

async function handleTopUpSubmit(e) {
    e.preventDefault();
    const amountVal = parseFloat(document.getElementById('topup-amount-input').value);
    const method = document.getElementById('topup-payment-method').value;

    if (!amountVal || amountVal < 50) {
        showToast('Minimum deposit amount is ₹50.00', 'warning');
        return;
    }

    const res = await API.post('/api/account/top-up', {
        amount: amountVal,
        paymentMethod: method
    });

    if (res.success && res.data) {
        showToast(`Successfully deposited ₹${amountVal.toFixed(2)} to your National Philately Account!`, 'success');
        closeTopUpModal();
        loadDashboardSummary();
    } else {
        showToast(res.message || 'Deposit top-up failed', 'error');
    }
}

// Transaction Ledger Modal
async function openTransactionHistoryModal() {
    const modal = document.getElementById('transactions-modal');
    const tbody = document.getElementById('transactions-tbody');
    modal.classList.add('active');

    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 24px;">Loading audit transactions...</td></tr>`;

    try {
        const res = await API.get('/api/account/transactions');
        if (res.success && res.data) {
            if (res.data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 24px; color: #64748b;">No transaction records found.</td></tr>`;
                return;
            }

            tbody.innerHTML = res.data.map(t => {
                const isCredit = t.transactionType.includes('CREDIT');
                return `
                    <tr>
                        <td><code style="font-size: 0.85rem;">${t.referenceNumber}</code></td>
                        <td>${new Date(t.transactionDate).toLocaleString()}</td>
                        <td style="color: ${isCredit ? 'var(--success)' : 'var(--danger)'}; font-weight: bold;">
                            ${isCredit ? '+' : '-'}₹${parseFloat(t.amount).toFixed(2)}
                        </td>
                        <td>₹${parseFloat(t.balanceAfter).toFixed(2)}</td>
                        <td><small>${t.description} (${t.paymentMethod})</small></td>
                    </tr>
                `;
            }).join('');
        }
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="5" style="color: red; text-align: center;">Failed to load transactions.</td></tr>`;
    }
}

function closeTransactionsModal() {
    document.getElementById('transactions-modal').classList.remove('active');
}

// Order Tracking Modal
async function viewOrderTracking(orderNumber) {
    const modal = document.getElementById('tracking-modal');
    const content = document.getElementById('tracking-modal-content');
    modal.classList.add('active');

    content.innerHTML = `<p style="text-align: center; padding: 24px;"><i class="fas fa-spinner fa-spin"></i> Fetching Speed Post consignment status...</p>`;

    try {
        const res = await API.get(`/api/orders/${orderNumber}`);
        if (res.success && res.data) {
            const o = res.data;
            const statuses = ['ORDER_PLACED', 'PROCESSING', 'SHIPPED', 'IN_TRANSIT', 'DELIVERED'];
            const currentIndex = statuses.indexOf(o.orderStatus);

            content.innerHTML = `
                <div style="background: #f8fafc; border: 1px solid var(--border-color); padding: 16px; border-radius: var(--radius-md); margin-bottom: 24px;">
                    <h4 style="margin: 0 0 6px 0; color: var(--primary); font-size: 1.05rem;">
                        <i class="fas fa-barcode"></i> Consignment Tracking: ${o.trackingNumber || 'Pending'}
                    </h4>
                    <p style="margin: 3px 0; font-size: 0.9rem;"><strong>Order Number:</strong> #${o.orderNumber}</p>
                    <p style="margin: 3px 0; font-size: 0.9rem;"><strong>Shipping Destination:</strong> ${o.shippingAddress}</p>
                    <p style="margin: 3px 0; font-size: 0.9rem; color: var(--text-muted);"><strong>Expected Delivery:</strong> ${o.expectedDeliveryDate}</p>
                </div>

                <div class="tracking-stepper">
                    ${statuses.map((s, idx) => {
                        const isDone = idx <= currentIndex;
                        const isCurrent = idx === currentIndex;
                        return `
                            <div class="step-point ${isDone ? 'done' : ''} ${isCurrent ? 'current' : ''}">
                                <div class="step-circle"><i class="fas fa-${isDone ? 'check' : 'circle'}"></i></div>
                                <div class="step-label">${s.replace('_', ' ')}</div>
                            </div>
                        `;
                    }).join('')}
                </div>

                <h4 style="margin-top: 24px; margin-bottom: 12px; color: var(--primary);">Ordered Philatelic Items:</h4>
                <ul style="list-style: none; padding: 0; margin: 0;">
                    ${(o.items || []).map(it => `
                        <li style="display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--border-color);">
                            <span>${it.item ? it.item.title : 'Item'} (x${it.quantity})</span>
                            <strong>₹${parseFloat(it.subtotal).toFixed(2)}</strong>
                        </li>
                    `).join('')}
                </ul>
            `;
        }
    } catch (e) {
        content.innerHTML = `<p style="color: red; text-align: center;">Failed to retrieve tracking details.</p>`;
    }
}

function closeTrackingModal() {
    document.getElementById('tracking-modal').classList.remove('active');
}

// User Profile Management
async function loadUserProfile() {
    try {
        const res = await API.get('/api/user/profile');
        if (res.success && res.data) {
            const u = res.data;
            if (document.getElementById('prof-fullname')) document.getElementById('prof-fullname').value = u.fullName || '';
            if (document.getElementById('prof-phone')) document.getElementById('prof-phone').value = u.phoneNumber || '';
            if (document.getElementById('prof-addr1')) document.getElementById('prof-addr1').value = u.addressLine1 || '';
            if (document.getElementById('prof-city')) document.getElementById('prof-city').value = u.city || '';
            if (document.getElementById('prof-state')) document.getElementById('prof-state').value = u.state || '';
            if (document.getElementById('prof-pincode')) document.getElementById('prof-pincode').value = u.pincode || '';
        }
    } catch (e) {
        console.error('Failed to load profile', e);
    }
}

function openProfileModal() {
    document.getElementById('profile-modal').classList.add('active');
}

function closeProfileModal() {
    document.getElementById('profile-modal').classList.remove('active');
}

async function handleProfileSubmit(e) {
    e.preventDefault();
    const payload = {
        fullName: document.getElementById('prof-fullname').value,
        phoneNumber: document.getElementById('prof-phone').value,
        addressLine1: document.getElementById('prof-addr1').value,
        city: document.getElementById('prof-city').value,
        state: document.getElementById('prof-state').value,
        pincode: document.getElementById('prof-pincode').value
    };

    const res = await API.put('/api/user/profile', payload);
    if (res.success && res.data) {
        showToast('Personal information & address updated successfully!', 'success');
        closeProfileModal();
        Auth.refreshProfile();
    } else {
        showToast(res.message || 'Failed to update profile', 'error');
    }
}

// Standing Instructions Modal
function openStandingInstructionsModal() {
    document.getElementById('instructions-modal').classList.add('active');
}

function closeInstructionsModal() {
    document.getElementById('instructions-modal').classList.remove('active');
}

async function handleInstructionsSubmit(e) {
    e.preventDefault();
    const checked = Array.from(document.querySelectorAll('input[name="standing-items"]:checked')).map(el => el.value);
    const notes = document.getElementById('standing-notes').value;
    const fullText = checked.join(', ') + (notes ? ` | Notes: ${notes}` : '');

    const res = await API.put('/api/account/instructions', { instructions: fullText });
    if (res.success) {
        showToast('Standing instructions updated for National PDA auto-dispatch!', 'success');
        closeInstructionsModal();
        loadDashboardSummary();
    } else {
        showToast('Failed to update instructions', 'error');
    }
}
