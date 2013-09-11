function sidebar() {
   var html = [
'     <div class="span3 sidebar">',
'       <div class="well" style="padding: 8px 0px; position: fixed;"">',
'         <ul class="nav nav-list">',
'           <li class="nav-header">Examples</li>',
'           <li><a href="list/index.html">Value Lists</a></li>',
'           <li><a href="table/index.html">Tables</a></li>',
'           <li><a href="pds3/index.html">PDS3</a></li>',
'           <li><a href="combined/index.html">Combinations</a></li>',
'           <li><a href="advanced/index.html">Advanced</a></li>',
'         </ul>',
'       </div><!-- well -->',
'      &nbsp; <!-- Content to keep span filled -->',
'   </div><!-- sidebar -->'
   ];
   for(x in html) { document.write(html[x] + "\n"); }
}