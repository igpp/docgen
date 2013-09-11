function header()
{
   var part = document.title.split("/", 2);
   var maintitle = part[0];
   var subtitle = '';
   if(part.length > 1) subtitle = part[1];
   
   var homepage = 'index.html';
   if(window.location.pathname.indexOf("index.html") !== -1) { homepage = '../index.html'; }
   
   var nav = [
'      <div class="span3">',
'         <div class="navigation">',
'            <ul>',
'               <li><a href="' + homepage + '">home</a></li>',
'            </ul>',
'         </div><!-- navigation -->',
'      </div><!-- span -->'
   ];
   
   var html = [
'   <div class="row header with-border">',
'      <div class="span9 logo">',
'         <h1><a href="index.html">' + maintitle,
' <span class="subtitle">' + subtitle + '</span></a></h1>',
'      </div>'
   ];
   
if(part.length > 1) { // Add nav area
   html.push(
'      <div class="span3">',
'         <div class="navigation">',
'            <ul>',
'               <li><a href="' + homepage + '">home</a></li>',
'            </ul>',
'         </div><!-- navigation -->',
'      </div><!-- span -->'
   );
}

html.push(
'   </div><!-- header -->',
'   <div class="row">'
);
   for(x in html) { document.write(html[x] + "\n"); }
}

function footer() {
   var html = [
'</div><!-- row -->',
'   <div class="row footer">',
'      <div class="span12">',
'        Copyright <a href="license.html">&copy;</a> 2013. Regents University of California.',
'      </div>',
'   </div>'
];
   for(x in html) { document.write(html[x] + "\n"); }
}