(function () {
  const clean = s => String(s || '').replace(/\s+/g, ' ').trim().slice(0, 500);
  const cfg = window.__adnirConfig || {};
  const output = [];
  const rating = /^\s*[0-5][.,]\d\s*(?:[★⭐☆\s]*\(?[\d,]+\)?|[★⭐☆])/;
  const action = /^(?:Call|Directions|Website|WhatsApp|Share|कॉल|दिशा|वेबसाइट|साझा करें)$/i;
  const phonePattern = /(?:\+91[\s().-]*|0)?[6-9](?:[\s().-]*\d){9}\b|\b0\d{2,4}[\s.-]?\d{6,8}\b/;
  const linesOf = el => (el.innerText || '').split(/\n+/).map(clean).filter(Boolean);
  const field = (el, selector) => { if (!selector) return ''; const n = el.querySelector(selector); return n ? clean(n.innerText || n.textContent || (n.getAttribute('href') || '').replace(/^tel:/, '')) : ''; };
  function parse(lines, element) {
    const index = lines.findIndex(s => rating.test(s));
    if (index < 1) return;
    const name = lines[index - 1];
    if (!name || name.length > 160 || action.test(name) || /^(Open|Closed|Closes|Opens|Search|More places|Places)\b/i.test(name)) return;
    const body = lines.slice(index + 1).join('\n');
    const tel = element && element.querySelector('a[href^="tel:"]');
    const phone = tel ? clean((tel.getAttribute('href') || '').slice(4).split('?')[0]) : (body.match(phonePattern) || [''])[0];
    let address = '';
    for (const raw of lines.slice(index + 1)) {
      if (action.test(raw) || /^(In-store|Delivery|Pickup|Pick-up|Service options|Reviews|Rating|"|“)/i.test(raw)) continue;
      let line = raw.replace(phone, '').trim();
      if (/^(Open|Closed|Opens|Closes|खुला|बंद)\b/i.test(line)) {
        line = line.split(/[·•]/).filter(s => !/\b(?:open|closed|opens|closes|am|pm|hours|24)\b|\d[:.]\d/i.test(s)).map(clean).join(' · ');
      }
      line = line.replace(/^[\s·•]+|[\s·•]+$/g, '');
      if (line && !rating.test(line) && !action.test(line) && (/[\d,]|\b(?:Rd|Road|Nagar|Colony|Street|Sector|Market|Khand|Marg|Near|Lucknow)\b/i.test(line) || line.length > 5)) { address = clean(line); break; }
    }
    output.push({ name, phone, address, source: location.href });
  }
  if (cfg.row) {
    const nodes = document.querySelectorAll(cfg.row);
    if (nodes.length > 3000) throw Error('Row selector बहुत अधिक items चुन रहा है');
    nodes.forEach(el => { if (!el.getClientRects().length) return; const name = field(el, cfg.name), phone = field(el, cfg.phone), address = field(el, cfg.address); if (name || phone || address) output.push({ name, phone, address, source: location.href }); });
  } else if (/(^|\.)google\./.test(location.hostname)) {
    // Mobile cards often have different class names. Locate their action controls,
    // then stop at the smallest parent containing a rating and a business name.
    const seeds = document.querySelectorAll('a[href^="tel:"], a, button, [role="button"], [role="heading"], h2, h3, .dbg0pd, .OSrXXb, .rllt__details');
    const visited = new Set();
    Array.from(seeds).slice(0, 5000).forEach(seed => {
      if (!seed.getClientRects().length) return;
      if (!seed.matches('[role="heading"], h2, h3, .dbg0pd, .OSrXXb, .rllt__details, a[href^="tel:"]') && !action.test(clean(seed.innerText || seed.textContent))) return;
      let el = seed;
      for (let depth = 0; el && depth < 9 && el !== document.body; depth++, el = el.parentElement) {
        const lines = linesOf(el);
        if (lines.length > 45) break;
        const indices = lines.map((s, i) => rating.test(s) ? i : -1).filter(i => i >= 0);
        if (indices.length === 1 && indices[0] > 0) {
          if (!visited.has(el)) { visited.add(el); parse(lines, el); }
          break;
        }
        if (indices.length > 1) break;
      }
    });
    // Text fallback supports mobile layouts without recognizable attributes.
    const lines = linesOf(document.body);
    const marks = lines.map((s, i) => rating.test(s) && i > 0 ? i : -1).filter(i => i >= 0);
    marks.forEach((at, n) => parse(lines.slice(at - 1, n + 1 < marks.length ? marks[n + 1] - 1 : Math.min(lines.length, at + 16)), null));
  } else { throw Error('इस साइट के लिए Advanced selectors भरें'); }
  const unique = new Map();
  output.forEach(x => { const key = x.name.toLowerCase(); const old = unique.get(key); if (!old) unique.set(key, x); else { if (!old.phone && x.phone) old.phone = x.phone; if (!old.address && x.address) old.address = x.address; } });
  return JSON.stringify(Array.from(unique.values()).slice(0, 300));
})()
