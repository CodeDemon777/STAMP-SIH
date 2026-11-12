// Special Cancellations Logic
let selectedCancellationId = null;

document.addEventListener('DOMContentLoaded', () => {
    loadSpecialCancellations();
    if (Auth.isLoggedIn()) {
        loadMyCancellationRequests();
    }
});

async function loadSpecialCancellations() {
    const container = document.getElementById('cancellations-container');
    if (!container) return;

    try {
        const res = await API.get('/api/cancellations');
        if (res.success && res.data) {
            container.innerHTML = res.data.map(sc => `
                <div class="cancellation-item">
                    <img src="${sc.imageUrl || 'https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=300'}" alt="${sc.title}">
                    <div class="cancellation-details">
                        <span class="badge" style="background: var(--info-bg); color: var(--info); margin-bottom: 8px;">
                            ${sc.postalCircle ? sc.postalCircle.name : 'All India'} • ${sc.postOfficeLocation || 'Head Post Office'}
                        </span>
                        <h3>${sc.title}</h3>
                        <p class="desc">${sc.description}</p>
                        <p class="date"><i class="far fa-calendar-alt"></i> Effective Date: <strong>${sc.cancellationDate}</strong></p>
                        <p class="code"><i class="fas fa-barcode"></i> Code: <code>${sc.cancellationCode}</code></p>
                        <button class="request-cancellation" onclick="openCancellationModal(${sc.id}, '${escapeHtml(sc.title)}')">
                            <i class="fas fa-stamp"></i> Request Cancellation on Purchased Items
                        </button>
                    </div>
                </div>
            `).join('');
        }
    } catch (e) {
        console.error('Failed to load cancellations', e);
    }
}

async function loadMyCancellationRequests() {
    const listContainer = document.getElementById('my-cancellation-requests-list');
    if (!listContainer) return;

    try {
        const res = await API.get('/api/cancellations/my-requests');
        if (res.success && res.data) {
            if (res.data.length === 0) {
                listContainer.innerHTML = `<p style="color: #64748b; padding: 10px;">No pending cancellation requests.</p>`;
                return;
            }

            listContainer.innerHTML = res.data.map(r => `
                <div style="background: white; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 14px; margin-bottom: 10px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                        <strong style="color: var(--primary);">${r.specialCancellation ? r.specialCancellation.title : 'Special Cancellation'}</strong>
                        <span class="badge" style="background: ${r.status === 'PROCESSED' ? 'var(--success-bg)' : r.status === 'APPROVED' ? 'var(--info-bg)' : 'var(--warning-bg)'}; color: var(--text-main);">
                            ${r.status}
                        </span>
                    </div>
                    <p style="margin: 4px 0; font-size: 0.85rem; color: var(--text-body);"><strong>Instructions:</strong> ${r.instructions}</p>
                    <small style="color: var(--text-muted);">Requested on: ${new Date(r.requestDate).toLocaleDateString()}</small>
                </div>
            `).join('');
        }
    } catch (e) {
        console.error('Failed to load my requests', e);
    }
}

function openCancellationModal(cancellationId, title) {
    if (!Auth.isLoggedIn()) {
        showToast('Please sign in to request official postmark cancellations.', 'warning');
        setTimeout(() => location.href = 'login.html', 800);
        return;
    }

    selectedCancellationId = cancellationId;
    document.getElementById('modal-cancellation-title').textContent = title;
    document.getElementById('cancellation-modal').classList.add('active');
}

function closeCancellationModal() {
    document.getElementById('cancellation-modal').classList.remove('active');
}

async function handleCancellationSubmit(e) {
    e.preventDefault();
    if (!selectedCancellationId) return;

    const instructions = document.getElementById('cancel-instructions').value;
    const orderNumber = document.getElementById('cancel-order-num').value;

    const payload = {
        specialCancellationId: selectedCancellationId,
        instructions: instructions || 'Please affix special pictorial cancellation mark on the right-hand corner of cover.'
    };

    const res = await API.post('/api/cancellations/request', payload);
    if (res.success && res.data) {
        showToast('Special cancellation request submitted to Postal Officer!', 'success');
        closeCancellationModal();
        loadMyCancellationRequests();
    } else {
        showToast(res.message || 'Submission failed', 'error');
    }
}

function escapeHtml(str) {
    return (str || '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
}

