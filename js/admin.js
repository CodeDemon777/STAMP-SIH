// Administrator & Postal Officer Portal Logic
document.addEventListener('DOMContentLoaded', () => {
    if (!Auth.isLoggedIn() || (!Auth.hasRole('ADMIN') && !Auth.hasRole('OFFICER'))) {
        showToast('Access Denied. Administrator or Postal Officer privileges required.', 'error');
        setTimeout(() => location.href = 'login.html', 1000);
        return;
    }

    loadMetrics();
    loadAdminItems();
    loadAdminOrders();
    loadAdminCancellationRequests();
    loadAdminCircles();
});

async function loadMetrics() {
    try {
        const res = await API.get('/api/admin/metrics');
        if (res.success && res.data) {
            const m = res.data;
            if (document.getElementById('metric-users')) document.getElementById('metric-users').textContent = m.totalUsers;
            if (document.getElementById('metric-orders')) document.getElementById('metric-orders').textContent = m.totalOrders;
            if (document.getElementById('metric-items')) document.getElementById('metric-items').textContent = m.totalItems;
            if (document.getElementById('metric-accounts')) document.getElementById('metric-accounts').textContent = m.totalAccounts;
        }
    } catch (e) {
        console.error('Failed to load metrics', e);
    }
}

async function loadAdminItems() {
    const tbody = document.getElementById('admin-items-tbody');
    if (!tbody) return;

    try {
        const res = await API.get('/api/items?size=50');
        if (res.success && res.data) {
            const items = res.data.content || [];
            tbody.innerHTML = items.map(it => `
                <tr>
                    <td><code>${it.itemCode}</code></td>
                    <td>
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <img src="${it.imageUrl}" style="width: 36px; height: 36px; object-fit: contain; background: #f8fafc; border-radius: 4px; border: 1px solid #e2e8f0;">
                            <span>${it.title}</span>
                        </div>
                    </td>
                    <td>${it.postalCircle ? it.postalCircle.name : '-'}</td>
                    <td>${it.materialType}</td>
                    <td><strong>₹${parseFloat(it.price).toFixed(2)}</strong></td>
                    <td><span class="badge ${it.stockQuantity > 0 ? 'badge-delivered' : 'badge-cancelled'}">${it.stockQuantity}</span></td>
                    <td>
                        <button class="btn-outline" style="padding: 4px 8px; color: var(--danger); border-color: var(--danger);" onclick="deleteItem(${it.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `).join('');
        }
    } catch (e) {
        console.error('Failed to load admin items', e);
    }
}

async function deleteItem(id) {
    if (!confirm('Are you sure you want to remove this philatelic item from the national catalog?')) return;
    const res = await API.delete(`/api/admin/items/${id}`);
    if (res.success) {
        showToast('Item deleted successfully', 'info');
        loadAdminItems();
        loadMetrics();
    } else {
        showToast(res.message || 'Failed to delete item', 'error');
    }
}

async function loadAdminOrders() {
    const tbody = document.getElementById('admin-orders-tbody');
    if (!tbody) return;

    try {
        const res = await API.get('/api/admin/orders');
        if (res.success && res.data) {
            tbody.innerHTML = res.data.map(o => `
                <tr>
                    <td><strong>#${o.orderNumber}</strong></td>
                    <td>${new Date(o.createdAt).toLocaleDateString()}</td>
                    <td>₹${parseFloat(o.totalAmount).toFixed(2)} (${o.paymentMethod})</td>
                    <td><code>${o.trackingNumber || 'Pending'}</code></td>
                    <td><span class="badge badge-${o.orderStatus.toLowerCase()}">${o.orderStatus}</span></td>
                    <td>
                        <select onchange="updateOrderStatus(${o.id}, this.value)" style="padding: 5px; border-radius: 4px; font-size: 0.85rem; border: 1px solid #cbd5e1;">
                            <option value="">Update Status...</option>
                            <option value="PROCESSING">Processing</option>
                            <option value="SHIPPED">Dispatched / Shipped</option>
                            <option value="IN_TRANSIT">In Transit</option>
                            <option value="DELIVERED">Delivered</option>
                            <option value="CANCELLED">Cancelled</option>
                        </select>
                    </td>
                </tr>
            `).join('');
        }
    } catch (e) {
        console.error('Failed to load admin orders', e);
    }
}

async function updateOrderStatus(orderId, status) {
    if (!status) return;
    const res = await API.put(`/api/admin/orders/${orderId}/status`, { status });
    if (res.success) {
        showToast(`Order status updated to ${status}`, 'success');
        loadAdminOrders();
    } else {
        showToast(res.message || 'Status update failed', 'error');
    }
}

async function loadAdminCancellationRequests() {
    const tbody = document.getElementById('admin-cancellations-tbody');
    if (!tbody) return;

    try {
        const res = await API.get('/api/admin/cancellations/requests');
        if (res.success && res.data) {
            tbody.innerHTML = res.data.map(r => `
                <tr>
                    <td><strong>#${r.id}</strong></td>
                    <td>${r.specialCancellation ? r.specialCancellation.title : '-'}</td>
                    <td>${r.instructions}</td>
                    <td><span class="badge" style="background: #f1f5f9;">${r.status}</span></td>
                    <td>
                        <div style="display: flex; gap: 6px;">
                            <button class="btn-primary" style="padding: 4px 8px; font-size: 0.8rem;" onclick="updateCancellationStatus(${r.id}, 'APPROVED')">
                                Approve
                            </button>
                            <button class="btn-secondary" style="padding: 4px 8px; font-size: 0.8rem;" onclick="updateCancellationStatus(${r.id}, 'PROCESSED')">
                                Process
                            </button>
                        </div>
                    </td>
                </tr>
            `).join('');
        }
    } catch (e) {
        console.error('Failed to load cancellation requests', e);
    }
}

async function updateCancellationStatus(reqId, status) {
    const res = await API.put(`/api/admin/cancellations/requests/${reqId}/status`, { status });
    if (res.success) {
        showToast(`Request updated to ${status}`, 'success');
        loadAdminCancellationRequests();
    }
}

async function loadAdminCircles() {
    const sel = document.getElementById('new-item-circle');
    if (!sel) return;
    const res = await API.get('/api/circles');
    if (res.success && res.data) {
        sel.innerHTML = res.data.map(c => `<option value="${c.id}">${c.name} (${c.code})</option>`).join('');
    }
}

function openNewItemModal() {
    document.getElementById('new-item-modal').classList.add('active');
}

function closeNewItemModal() {
    document.getElementById('new-item-modal').classList.remove('active');
}

async function handleNewItemSubmit(e) {
    e.preventDefault();
    const payload = {
        title: document.getElementById('new-item-title').value,
        itemCode: document.getElementById('new-item-code').value,
        postalCircleId: parseInt(document.getElementById('new-item-circle').value),
        materialType: document.getElementById('new-item-type').value,
        theme: document.getElementById('new-item-theme').value,
        releaseYear: parseInt(document.getElementById('new-item-year').value),
        price: parseFloat(document.getElementById('new-item-price').value),
        denomination: document.getElementById('new-item-denom').value || '₹ 10.00',
        stockQuantity: parseInt(document.getElementById('new-item-stock').value) || 50,
        imageUrl: document.getElementById('new-item-image').value,
        specialFeatures: document.getElementById('new-item-features').value,
        description: document.getElementById('new-item-desc').value,
        featured: document.getElementById('new-item-featured').checked,
        popular: document.getElementById('new-item-popular').checked
    };

    const res = await API.post('/api/admin/items', payload);
    if (res.success) {
        showToast('New Philatelic material added to national catalog!', 'success');
        closeNewItemModal();
        loadAdminItems();
        loadMetrics();
    } else {
        showToast(res.message || 'Failed to add item', 'error');
    }
}

