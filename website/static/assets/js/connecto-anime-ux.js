/**
 * Connecto & Our Store - Anime.js v4 Motion & UX Engine
 * Powered by Anime.js (https://github.com/juliangarnier/anime.git)
 * Delivers 120fps hardware-accelerated spring physics, staggered entrances,
 * tactile micro-interactions, and reactive transitions.
 */

(function initConnectoAnimeEngine() {
  'use strict';

  function waitForAnime(callback, attempts = 0) {
    if (typeof window.anime !== 'undefined' && typeof window.anime.animate === 'function') {
      callback(window.anime);
    } else if (attempts < 50) {
      setTimeout(() => waitForAnime(callback, attempts + 1), 50);
    }
  }

  waitForAnime((anime) => {
    const { animate, createTimeline, stagger, spring, utils } = anime;

    // Check for reduced motion preference
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (prefersReducedMotion) {
      console.log('[Anime.js UX] Reduced motion preferred; skipping motion effects.');
      return;
    }

    console.log('[Anime.js UX] Initializing Connecto & Our Store Motion Engine v4.5...');

    /* ==========================================================================
       1. CONNECTO HERO & ENTRANCE CHOREOGRAPHY
       ========================================================================== */
    function initHeroChoreography() {
      const heroTitle = document.querySelector('.hero-official-title');
      const heroDesc = document.querySelector('.hero-official-desc');
      const heroBadges = document.querySelectorAll('.hero-pill-badge');
      const heroButtons = document.querySelectorAll('.hero-cta-buttons-row .btn');
      const heroMockup = document.getElementById('heroChatMockup');

      if (!heroTitle) return;

      const heroTl = createTimeline({
        defaults: {
          ease: 'outExpo',
          duration: 900
        }
      });

      heroTl
        .add(heroTitle, {
          y: [30, 0],
          opacity: [0, 1],
          duration: 800
        }, 100)
        .add(heroDesc, {
          y: [20, 0],
          opacity: [0, 1],
          duration: 700
        }, '-=600')
        .add(heroBadges, {
          scale: [0.85, 1],
          y: [15, 0],
          opacity: [0, 1],
          delay: stagger(60),
          ease: spring({ stiffness: 200, damping: 15 })
        }, '-=400')
        .add(heroButtons, {
          scale: [0.9, 1],
          opacity: [0, 1],
          delay: stagger(70),
          ease: 'outBack(1.4)'
        }, '-=350');

      if (heroMockup) {
        heroTl.add(heroMockup, {
          y: [40, 0],
          scale: [0.96, 1],
          opacity: [0, 1],
          duration: 1000,
          ease: 'outQuart'
        }, 300);
      }
    }

    /* ==========================================================================
       2. CONNECTO LIVE CHAT SANDBOX MICRO-INTERACTIONS
       ========================================================================== */
    function initSandboxInteractions() {
      // Traffic control window dots micro-bounce
      const dots = document.querySelectorAll('.mock-win-controls .win-dot');
      dots.forEach((dot, idx) => {
        dot.addEventListener('mouseenter', () => {
          animate(dot, {
            scale: [1, 1.35, 1],
            duration: 350,
            ease: 'outBack(2)'
          });
        });
      });

      // Hook into demo message insertions
      const origInsertMsg = window.insertSandboxMessage;
      if (typeof origInsertMsg === 'function') {
        window.insertSandboxMessage = function(...args) {
          const res = origInsertMsg.apply(this, args);
          animateLatestSandboxMessage();
          return res;
        };
      }

      // Intercept send message
      const origSendMsg = window.sendSandboxDemoMessage;
      if (typeof origSendMsg === 'function') {
        window.sendSandboxDemoMessage = function(...args) {
          const res = origSendMsg.apply(this, args);
          animateLatestSandboxMessage();
          return res;
        };
      }

      function animateLatestSandboxMessage() {
        const msgList = document.getElementById('mockMsgList');
        if (!msgList) return;
        const lastMsg = msgList.lastElementChild;
        if (lastMsg && !lastMsg.dataset.animeAnimated) {
          lastMsg.dataset.animeAnimated = 'true';
          animate(lastMsg, {
            y: [16, 0],
            scale: [0.94, 1],
            opacity: [0, 1],
            duration: 400,
            ease: spring({ mass: 1, stiffness: 220, damping: 16 })
          });
        }
      }

      // Reaction demo emojis tactile bounce
      const demoBtns = document.querySelectorAll('.mock-interactive-input-row .mock-demo-btn');
      demoBtns.forEach(btn => {
        btn.addEventListener('click', () => {
          animate(btn, {
            scale: [0.75, 1.25, 1],
            rotate: [0, -12, 12, 0],
            duration: 450,
            ease: 'outBack(1.8)'
          });
        });
      });
    }

    /* ==========================================================================
       3. OUR STORE UX SHOWCASE: STAGGERED REVEALS & TACTILE SPRINGS
       ========================================================================== */
    function initOurStoreShowcase() {
      const storeSection = document.getElementById('ourStoreSection');
      if (!storeSection) return;

      const releaseBadge = storeSection.querySelector('.android-release-tag-row');
      const storeTitle = storeSection.querySelector('.android-showcase-title');
      const storeDesc = storeSection.querySelector('.android-showcase-desc');
      const specItems = storeSection.querySelectorAll('.android-spec-item');
      const featCards = storeSection.querySelectorAll('.android-feat-card');
      const ctaBtn = storeSection.querySelector('.android-primary-download-btn');
      const phoneMockup = storeSection.querySelector('.android-phone-mockup-wrap > div');
      const highlightCards = storeSection.querySelectorAll('.android-phone-mockup-wrap div[style*="border-radius: 12px"]');

      // Scroll-triggered Reveal Observer
      let storeAnimated = false;
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting && !storeAnimated) {
            storeAnimated = true;
            observer.unobserve(entry.target);

            const storeTl = createTimeline({
              defaults: {
                ease: 'outExpo',
                duration: 750
              }
            });

            if (releaseBadge) {
              storeTl.add(releaseBadge, {
                y: [-20, 0],
                opacity: [0, 1],
                duration: 600
              }, 0);
            }

            if (storeTitle) {
              storeTl.add(storeTitle, {
                y: [20, 0],
                opacity: [0, 1]
              }, '-=400');
            }

            if (specItems && specItems.length) {
              storeTl.add(specItems, {
                scale: [0.82, 1],
                opacity: [0, 1],
                delay: stagger(70),
                ease: spring({ stiffness: 220, damping: 16 })
              }, '-=300');
            }

            if (featCards && featCards.length) {
              storeTl.add(featCards, {
                x: [-24, 0],
                opacity: [0, 1],
                delay: stagger(60),
                ease: 'outBack(1.2)'
              }, '-=350');
            }

            if (phoneMockup) {
              storeTl.add(phoneMockup, {
                scale: [0.93, 1],
                y: [30, 0],
                opacity: [0, 1],
                duration: 850,
                ease: spring({ stiffness: 180, damping: 15 })
              }, 200);
            }

            if (ctaBtn) {
              storeTl.add(ctaBtn, {
                scale: [0.9, 1],
                opacity: [0, 1],
                ease: 'outBack(1.5)'
              }, '-=200');
            }
          }
        });
      }, { threshold: 0.15 });

      observer.observe(storeSection);

      // Interactive Spring Hover on Our Store Feature Cards
      featCards.forEach(card => {
        card.addEventListener('mouseenter', () => {
          animate(card, {
            y: -6,
            scale: 1.015,
            duration: 300,
            ease: spring({ mass: 1, stiffness: 240, damping: 16 })
          });
        });
        card.addEventListener('mouseleave', () => {
          animate(card, {
            y: 0,
            scale: 1,
            duration: 350,
            ease: spring({ mass: 1, stiffness: 200, damping: 18 })
          });
        });
      });

      // Interactive Spring Hover on Right Hub Highlight Cards
      highlightCards.forEach(card => {
        card.style.transition = 'none'; // prevent CSS transition conflict
        card.addEventListener('mouseenter', () => {
          animate(card, {
            x: 6,
            scale: 1.025,
            duration: 280,
            ease: spring({ mass: 1, stiffness: 260, damping: 16 })
          });
        });
        card.addEventListener('mouseleave', () => {
          animate(card, {
            x: 0,
            scale: 1,
            duration: 320,
            ease: spring({ mass: 1, stiffness: 200, damping: 18 })
          });
        });
      });

      // Our Store Download CTA Button: Tactile Spring Click Feedback
      if (ctaBtn) {
        ctaBtn.addEventListener('mousedown', () => {
          animate(ctaBtn, {
            scale: 0.95,
            duration: 120,
            ease: 'outQuad'
          });
        });
        ctaBtn.addEventListener('mouseup', () => {
          animate(ctaBtn, {
            scale: [0.95, 1.03, 1],
            duration: 400,
            ease: spring({ mass: 1, stiffness: 280, damping: 14 })
          });
        });
      }
    }

    /* ==========================================================================
       4. UNIVERSAL INTERACTIVE CARDS SPRING HOVER
       ========================================================================== */
    function initCardSpringPhysics() {
      const bentoCards = document.querySelectorAll('.ecosystem-card, .step-card-custom, .comparison-card');
      bentoCards.forEach(card => {
        card.addEventListener('mouseenter', () => {
          animate(card, {
            y: -5,
            duration: 300,
            ease: spring({ stiffness: 220, damping: 17 })
          });
        });
        card.addEventListener('mouseleave', () => {
          animate(card, {
            y: 0,
            duration: 350,
            ease: spring({ stiffness: 180, damping: 18 })
          });
        });
      });
    }

    /* ==========================================================================
       5. MODAL SYSTEM SPRING DIALOG TRANSITIONS
       ========================================================================== */
    function initModalSpringTransitions() {
      const modalOverlays = document.querySelectorAll('.modal-overlay');
      modalOverlays.forEach(overlay => {
        // Observe display style mutations
        const observer = new MutationObserver(() => {
          const isVisible = overlay.style.display !== 'none' && overlay.style.display !== '';
          if (isVisible && !overlay.dataset.animeOpen) {
            overlay.dataset.animeOpen = 'true';
            const modalBox = overlay.querySelector('.modal-box, .modal-box-scroll, .auth-modal-box');
            if (modalBox) {
              animate(overlay, {
                opacity: [0, 1],
                duration: 250,
                ease: 'outQuad'
              });
              animate(modalBox, {
                scale: [0.92, 1],
                y: [24, 0],
                opacity: [0, 1],
                duration: 450,
                ease: spring({ mass: 1, stiffness: 240, damping: 18 })
              });
            }
          } else if (!isVisible) {
            delete overlay.dataset.animeOpen;
          }
        });
        observer.observe(overlay, { attributes: true, attributeFilter: ['style', 'class'] });
      });
    }

    // Run all modules
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', () => {
        initHeroChoreography();
        initSandboxInteractions();
        initOurStoreShowcase();
        initCardSpringPhysics();
        initModalSpringTransitions();
      });
    } else {
      initHeroChoreography();
      initSandboxInteractions();
      initOurStoreShowcase();
      initCardSpringPhysics();
      initModalSpringTransitions();
    }
  });
})();
