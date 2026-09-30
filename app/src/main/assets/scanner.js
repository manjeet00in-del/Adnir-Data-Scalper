(function(){
  const clean = s => String(s || '').replace(/\s+/g,' ').trim().slice(0,500);
  const visible = e => !!(e && e.getClientRects().length);
  const cfg = window.__adnirConfig || {};
  const output=[];
  const field=(el,selector) => {if(!selector)return ''; const n=el.querySelector(selector);if(!n)return '';return clean(n.innerText || n.textContent || (n.getAttribute('href')||'').replace(/^tel:/,''));};
  if(cfg.row){
    const nodes=document.querySelectorAll(cfg.row);
    if(nodes.length>3000)throw Error('Row selector बहुत अधिक items चुन रहा है');
    nodes.forEach(el => {if(!visible(el))return;const name=field(el,cfg.name),phone=field(el,cfg.phone),address=field(el,cfg.address);if(name||phone||address)output.push({name,phone,address,source:location.href});});
  } else if(/(^|\.)google\./.test(location.hostname)){
    const nodes=document.querySelectorAll('.dbg0pd, .OSrXXb, .rllt__details > div:first-child');
    nodes.forEach(node => {
      if(!visible(node))return;
      const name=clean(node.innerText || node.textContent);
      if(!name || name.length>100)return;
      const card=node.closest('.VkpGBb, .cXedhc, [data-cid], .rllt__details') || node.parentElement?.parentElement;
      if(!card)return;
      const body=card.innerText || '';
      const lines=body.split(/\n+/).map(s=>s.trim()).filter(Boolean);
      const phone=(body.match(/(?:\+91[\s.-]?|0)?[6-9]\d{4}[\s.-]?\d{5}\b|\b0\d{2,4}[\s.-]?\d{6,8}\b/)||[])[0]||'';
      const candidate=lines.find(line=>line!==name && !/^\d(?:\.\d)?\s*(?:\(|★)/.test(line) && !/^(Open|Closed|Opens|Closes|In-store|Delivery|Pickup|Website|Directions|Call)\b/i.test(line) && (line.includes('·') || /\b(?:Rd|Road|Nagar|Colony|Lucknow|Street|Sector|Market|Khand|Marg|Near)\b/i.test(line)));
      const address=clean((candidate||'').replace(phone,'').replace(/\s*[·•]\s*$/,''));
      if(address||phone)output.push({name,phone,address,source:location.href});
    });
  } else {throw Error('इस साइट के लिए Advanced selectors भरें');}
  return JSON.stringify([...new Map(output.map(x=>[x.name.toLowerCase()+'|'+x.phone+'|'+x.source,x])).values()].slice(0,300));
})()
