/* ============================================================
   Conference Management System - script.js
   ============================================================ */

document.addEventListener('DOMContentLoaded', () => {

  // ============================================================
  // Auto-dismiss alerts after 4 seconds
  // ============================================================
  const alerts = document.querySelectorAll('.alert-cms');
  alerts.forEach(alert => {
    setTimeout(() => {
      alert.style.transition = 'opacity 0.5s ease';
      alert.style.opacity = '0';
      setTimeout(() => alert.remove(), 500);
    }, 4000);
  });

  // ============================================================
  // Confirm before destructive actions
  // ============================================================
  document.querySelectorAll('[data-confirm]').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const message = btn.dataset.confirm || 'Are you sure?';
      if (!confirm(message)) {
        e.preventDefault();
      }
    });
  });

  // ============================================================
  // File input label update
  // ============================================================
  const fileInput = document.getElementById('fileInput');
  const fileLabel = document.getElementById('fileLabel');
  if (fileInput && fileLabel) {
    fileInput.addEventListener('change', () => {
      const file = fileInput.files[0];
      fileLabel.textContent = file ? file.name : 'Choose PDF file...';
    });
  }

  // ============================================================
  // Search debounce (optional live search trigger)
  // ============================================================
  const searchInput = document.getElementById('searchInput');
  if (searchInput) {
    let timeout;
    searchInput.addEventListener('input', () => {
      clearTimeout(timeout);
      timeout = setTimeout(() => {
        searchInput.closest('form').submit();
      }, 600);
    });
  }

  // ============================================================
  // Navbar active link highlighting
  // ============================================================
  const currentPath = window.location.pathname;
  document.querySelectorAll('.nav-link').forEach(link => {
    if (link.getAttribute('href') === currentPath) {
      link.classList.add('active');
    }
  });

  // ============================================================
  // Fade-up animation for cards
  // ============================================================
  const cards = document.querySelectorAll('.cms-card, .stat-card');
  cards.forEach((card, index) => {
    card.style.animationDelay = `${index * 0.07}s`;
    card.classList.add('fade-up');
  });

});
