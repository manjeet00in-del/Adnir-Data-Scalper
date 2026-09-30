const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const script=fs.readFileSync('app/src/main/assets/scanner.js','utf8');
const lines=['VANI NARAYAN MEDICAL STORE & CLINIC','4.9 ⭐ (74) · Medical supply store','Open · Closes 10:30 pm · near POLICE CHAWKI','In-store shopping · Delivery','Call','Directions','Ply Mart','4.7 (152) · Hardware store','104-105, Cp-1 · 098398 66694','Open · Closes 8 pm','Call'];
const body={innerText:lines.join('\n')};
const card={innerText:lines.slice(0,6).join('\n'),parentElement:body,querySelector:()=>({getAttribute:()=> 'tel:09839866694'})};
const seed={innerText:'Call',textContent:'Call',getClientRects:()=>[1],matches:()=>true,parentElement:card};
const context={window:{},location:{hostname:'www.google.com',href:'https://www.google.com/search?q=test'},document:{body,querySelectorAll:()=>[seed]}};
const data=JSON.parse(vm.runInNewContext(script,context));
assert.equal(data.length,2);assert.equal(data[0].name,lines[0]);assert.equal(data[0].phone,'09839866694');assert.equal(data[0].address,'near POLICE CHAWKI');assert.equal(data[1].name,'Ply Mart');assert.equal(data[1].phone,'098398 66694');assert.equal(data[1].address,'104-105, Cp-1');
// A listing with no published number must remain blank, and no rating match returns no data.
context.document.querySelectorAll=()=>[];context.document.body={innerText:'Alpha Hardware\n4.6 (25) · Hardware store\nSector 4, Lucknow\nOpen · Closes 8 pm\nCall'};
const noPhone=JSON.parse(vm.runInNewContext(script,context));assert.equal(noPhone.length,1);assert.equal(noPhone[0].phone,'');
context.document.body={innerText:'Search results\nordinary unrelated article'};assert.equal(JSON.parse(vm.runInNewContext(script,context)).length,0);
console.log('Mobile cards, inline hours/address, tel links, dedupe, missing numbers and empty results passed');
