// Product Detail Page Logic
let currentItem = null;
let currentQuantity = 1;

document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    const itemId = params.get('id') || 1;
    loadItemDetails(itemId);
    loadRelatedItems(itemId);
});

async function loadItemDetails(id) {
    try {
        const res = await API.get(`/api/items/${id}`);
        if (res.success && res.data) {
            currentItem = res.data;
            renderItem(currentItem);
        } else {
            const container = document.querySelector('.container');
            if (container) {
                container.innerHTML = `<div style="grid-column: 1 / -1; padding: 100px 20px; text-align: center;"><h2>Philatelic Item not found</h2><a href="Philatelic Materials.html" class="btn-primary" style="margin-top: 16px;"><i class="fas fa-arrow-left"></i> Back to Catalog</a></div>`;
            }
        }
    } catch (e) {
        console.error('Failed to load item', e);
    }
}

function renderItem(item) {
    document.title = `${item.title} - India Post Philately`;

    const titleEl = document.getElementById('item-title');
    const circleEl = document.getElementById('item-circle');
    const dateEl = document.getElementById('item-date');
    const featuresEl = document.getElementById('item-features');
    const descEl = document.getElementById('item-desc');
    const priceEl = document.getElementById('item-price');
    const availEl = document.getElementById('item-avail');
    const mainImgEl = document.getElementById('main-item-image');

    if (titleEl) titleEl.textContent = item.title;
    if (circleEl) circleEl.textContent = item.postalCircle ? `${item.postalCircle.name} (${item.postalCircle.headquarters})` : 'All India Circle';
    if (dateEl) dateEl.textContent = item.releaseDate || item.releaseYear;
    if (featuresEl) featuresEl.textContent = item.specialFeatures || 'Official Commemorative Release';
    if (descEl) descEl.textContent = item.description || '';
    if (priceEl) priceEl.textContent = `₹${parseFloat(item.price).toFixed(2)}`;
    
    if (availEl) {
        if (item.stockQuantity > 0) {
            availEl.innerHTML = `<span style="color: var(--success); font-weight: 700; display: inline-flex; align-items: center; gap: 6px;"><i class="fas fa-check-circle"></i> In Stock (${item.stockQuantity} units available)</span>`;
        } else {
            availEl.innerHTML = `<span style="color: var(--danger); font-weight: 700; display: inline-flex; align-items: center; gap: 6px;"><i class="fas fa-times-circle"></i> Out of Stock</span>`;
        }
    }

    if (mainImgEl) {
        mainImgEl.src = item.imageUrl || 'https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=600';
    }

    const thumbContainer = document.getElementById('item-thumbnails');
    if (thumbContainer) {
        thumbContainer.innerHTML = `
            <img src="${item.imageUrl}" class="thumbnail active" onclick="switchImage('${item.imageUrl}', this)">
            <img src="https://images.unsplash.com/photo-1561731216-c3a4d99437d5?w=300" class="thumbnail" onclick="switchImage('https://images.unsplash.com/photo-1561731216-c3a4d99437d5?w=600', this)">
            <img src="https://images.unsplash.com/photo-1600100397608-f010e42e4e75?w=300" class="thumbnail" onclick="switchImage('https://images.unsplash.com/photo-1600100397608-f010e42e4e75?w=600', this)">
        `;
    }
}

function switchImage(src, thumbEl) {
    const mainImg = document.getElementById('main-item-image');
    if (mainImg) mainImg.src = src;
    document.querySelectorAll('.thumbnail').forEach(t => t.classList.remove('active'));
    if (thumbEl) thumbEl.classList.add('active');
}

function updateQuantity(delta) {
    currentQuantity = Math.max(1, currentQuantity + delta);
    const qtyInput = document.getElementById('order-qty-input');
    if (qtyInput) qtyInput.value = currentQuantity;
}

async function orderNow(paymentMethod = 'PDA_WALLET') {
    if (!currentItem) return;

    if (!Auth.isLoggedIn()) {
        showToast('Please sign in with your Philatelic Deposit Account to order', 'warning');
        setTimeout(() => location.href = 'login.html', 800);
        return;
    }

    const qtyInput = document.getElementById('order-qty-input');
    const qty = qtyInput ? parseInt(qtyInput.value) || 1 : 1;
    const totalCost = currentItem.price * qty;

    const confirmMsg = `Confirm order of ${qty}x "${currentItem.title}" for ₹${totalCost.toFixed(2)} using ${paymentMethod === 'PDA_WALLET' ? 'your Philatelic Deposit Account balance' : paymentMethod}?`;
    if (!confirm(confirmMsg)) return;

    const res = await API.post('/api/orders', {
        paymentMethod: paymentMethod,
        items: [{ itemId: currentItem.id, quantity: qty }]
    });

    if (res.success && res.data) {
        showToast(`Order Placed Successfully! Speed Post Consignment No: ${res.data.trackingNumber}`, 'success');
        Auth.refreshProfile();
        setTimeout(() => location.href = 'Deposit Account.html', 1500);
    } else {
        showToast(res.message || 'Order failed. Please check your PDA balance.', 'error');
    }
}

async function addToWishlist() {
    if (!currentItem) return;
    if (!Auth.isLoggedIn()) {
        showToast('Please sign in to save items to your wishlist', 'warning');
        return;
    }

    const res = await API.post(`/api/wishlist/${currentItem.id}`);
    if (res.success) {
        showToast(`"${currentItem.title}" added to your Wishlist!`, 'success');
    } else {
        showToast(res.message || 'Could not add to wishlist', 'error');
    }
}

async function loadRelatedItems(id) {
    const container = document.getElementById('related-items-container');
    if (!container) return;

    try {
        const res = await API.get(`/api/items/${id}/related`);
        if (res.success && res.data && res.data.length > 0) {
            container.innerHTML = res.data.slice(0, 4).map(it => `
                <div class="related-item" onclick="location.href='Product Detail.html?id=${it.id}'">
                    <img src="${it.imageUrl || 'https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=300'}" alt="${it.title}">
                    <p><strong>${it.title}</strong></p>
                    <span style="color: var(--secondary); font-weight: bold;">₹${parseFloat(it.price).toFixed(2)}</span>
                    <button class="view-item" onclick="event.stopPropagation(); location.href='Product Detail.html?id=${it.id}'">
                        View Item
                    </button>
                </div>
            `).join('');
        }
    } catch (e) {
        console.error('Failed to load related items', e);
    }
}
