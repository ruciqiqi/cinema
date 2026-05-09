// ==================== 全局状态 ====================
var API = '/api';
var S = {
  token: localStorage.getItem('cinema_token'),
  username: localStorage.getItem('cinema_username'),
  role: localStorage.getItem('cinema_role'),
  movieId: null, showtimeId: null, hallId: null,
  selectedSeats: [], showtime: null, snackCart: {},
  currentGenre: 'all', currentSort: 'default',
  allMovies: [], allShowtimes: [], currentDate: '',
  cinemas: [], currentCinemaId: null,
  heroIdx: 0, heroTimer: null
};

// ==================== Utilities ====================
function $(id) { return document.getElementById(id); }
function esc(s) { if(!s)return''; return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }
function escA(s) { if(!s)return''; return String(s).replace(/&/g,'&amp;').replace(/"/g,'&quot;').replace(/'/g,'&#39;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }
function fmtDt(d) { if(!d)return''; var p=d.split('-'); if(p.length===3){var dt=new Date(+p[0],+p[1]-1,+p[2]); var w=['周日','周一','周二','周三','周四','周五','周六']; return p[1]+'月'+p[2]+'日 '+w[dt.getDay()]; } return d; }
function authHdrs() { var h={'Content-Type':'application/json'}; if(S.token)h['Authorization']='Bearer '+S.token; return h; }
function isLogin() { return !!S.token; }
function isAdmin() { return S.role==='admin'; }
function fetchJ(url, opts) { return fetch(url, opts).then(function(r){ return r.json(); }); }

// ==================== Toast ====================
function toast(msg, type) {
  type = type || 'info';
  var t = document.createElement('div');
  t.className = 'toast ' + type;
  t.textContent = msg;
  $('toastContainer').appendChild(t);
  setTimeout(function(){ t.style.animation='slideOut .3s ease forwards'; setTimeout(function(){ t.remove(); },300); }, 2500);
}

// ==================== Navigation ====================
function navTo(page) {
  document.querySelectorAll('.page').forEach(function(p){ p.classList.remove('active'); });
  var el = $('page-' + page);
  if (el) el.classList.add('active');
  document.querySelectorAll('.nav-links a').forEach(function(a){ a.classList.remove('nav-active'); });
  var navLink = document.querySelector('.nav-links a[href="#'+page+'"]');
  if (navLink) navLink.classList.add('nav-active');

  if (page === 'home') loadHome();
  else if (page === 'movies') loadMoviesPage();
  else if (page === 'user') loadUserCenter();
  else if (page === 'admin') loadAdmin();
  window.location.hash = page;
}

window.onhashchange = function() {
  var p = (window.location.hash || '#home').replace('#', '');
  navTo(p);
};

// ==================== Auth ====================
function showAuth() { $('authModal').style.display='flex'; $('authMsg').textContent=''; }
function closeAuth() { $('authModal').style.display='none'; }
function switchAuthTab(tab, el) {
  document.querySelectorAll('.modal-tab').forEach(function(t){ t.classList.remove('active'); });
  el.classList.add('active');
  $('formLogin').style.display = tab==='login'?'flex':'none';
  $('formRegister').style.display = tab==='register'?'flex':'none';
  $('authMsg').textContent='';
}
function doLogin() {
  var u=$('loginUser').value.trim(), p=$('loginPass').value.trim();
  if(!u||!p){$('authMsg').textContent='请填写用户名和密码';return;}
  fetchJ(API+'/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username:u,password:p})}).then(function(r){
    if(r.success){ saveAuth(r); closeAuth(); navTo('home'); toast('登录成功','success'); }
    else $('authMsg').textContent=r.message;
  });
}
function doRegister() {
  var u=$('regUser').value.trim(), p=$('regPass').value.trim(), ph=$('regPhone').value.trim();
  if(!u||!p){$('authMsg').textContent='请填写用户名和密码';return;}
  if(u.length<3){$('authMsg').textContent='用户名至少3个字符';return;}
  if(p.length<6){$('authMsg').textContent='密码至少6个字符';return;}
  fetchJ(API+'/auth/register',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username:u,password:p,phone:ph})}).then(function(r){
    if(r.success){ saveAuth(r); closeAuth(); navTo('home'); toast('注册成功！','success'); }
    else $('authMsg').textContent=r.message;
  });
}
function saveAuth(r) {
  S.token=r.token; S.username=r.username; S.role=r.role;
  localStorage.setItem('cinema_token',r.token); localStorage.setItem('cinema_username',r.username); localStorage.setItem('cinema_role',r.role);
  updateUI();
}
function doLogout() {
  S.token=null; S.username=null; S.role=null;
  localStorage.removeItem('cinema_token'); localStorage.removeItem('cinema_username'); localStorage.removeItem('cinema_role');
  updateUI(); navTo('home'); toast('已退出登录');
}
function updateUI() {
  var li=isLogin();
  $('btnLogin').style.display=li?'none':'inline-block';
  $('userInfo').style.display=li?'flex':'none';
  $('navUserCenter').style.display=(li&&!isAdmin())?'inline':'none';
  $('navAdmin').style.display=isAdmin()?'inline':'none';
  if(li) $('userNameDisplay').textContent=S.username+(isAdmin()?'(管理员)':'');
}

// ==================== Cinemas ====================
function loadCinemas() {
  fetchJ(API+'/cinemas').then(function(r){
    if(r.success && r.data.length) {
      S.cinemas = r.data;
      if (!S.currentCinemaId) S.currentCinemaId = r.data[0].id;
      var sel = $('cinemaSelect');
      sel.innerHTML = r.data.map(function(c){ return '<option value="'+c.id+'">'+esc(c.name)+'</option>'; }).join('');
      sel.value = S.currentCinemaId;
    }
  });
}
function switchCinema() { S.currentCinemaId = +$('cinemaSelect').value; navTo('home'); }

// ==================== Home ====================
function loadHome() {
  loadCinemas();
  fetchJ(API+'/movies').then(function(r){
    if(!r.success) return;
    S.allMovies = r.data;
    var hot = r.data.filter(function(m){ return m.isHot || m.rating>=8.8; });
    if(!hot.length) hot = r.data.slice(0,6);
    // Hero
    if (hot.length > 1) {
      S.heroMovies = hot.slice(0,4);
      S.heroIdx = 0;
      renderHero();
      if (S.heroTimer) clearInterval(S.heroTimer);
      S.heroTimer = setInterval(function(){ S.heroIdx=(S.heroIdx+1)%S.heroMovies.length; renderHeroSlide(); }, 4000);
    }
    // Hot movies scroll
    $('hotMovies').innerHTML = r.data.filter(function(m){ return m.isHot || m.status==='showing'; }).slice(0,10).map(renderMovieCard).join('');
  });
  // Announcements
  fetchJ(API+'/announcements').then(function(r){
    if(r.success && r.data.length) {
      $('announcementList').innerHTML = r.data.slice(0,3).map(function(a){
        return '<div class="announcement-item"><span class="an-icon">📢</span><div class="an-text"><strong>'+esc(a.title)+'</strong><span>'+esc(a.content||'')+'</span></div></div>';
      }).join('');
    }
  });
}
function renderHero() {
  var m = S.heroMovies;
  $('heroSlides').innerHTML = m.map(function(mv,i){
    return '<div class="hero-slide'+(i===0?' active':'')+'" style="background:'+(mv.posterBg||'#1a1a2e')+';"></div>';
  }).join('');
  $('heroDots').innerHTML = m.map(function(_,i){
    return '<span class="hero-dot'+(i===0?' active':'')+'" onclick="goHero('+i+')"></span>';
  }).join('');
  updateHeroInfo(0);
}
function renderHeroSlide() {
  var slides = document.querySelectorAll('.hero-slide');
  var dots = document.querySelectorAll('.hero-dot');
  slides.forEach(function(s,i){ s.classList.toggle('active',i===S.heroIdx); });
  dots.forEach(function(d,i){ d.classList.toggle('active',i===S.heroIdx); });
  updateHeroInfo(S.heroIdx);
}
function goHero(i) { S.heroIdx=i; renderHeroSlide(); }
function updateHeroInfo(i) {
  var m = S.heroMovies[i];
  $('heroTitle').textContent = m.title;
  $('heroDesc').textContent = (m.description||'').substring(0,80)+'...';
  $('heroBtn').onclick = function(){ showDetail(m.id); };
}

// ==================== Movies Page ====================
function loadMoviesPage() {
  fetchJ(API+'/movies').then(function(r){
    if(!r.success) return;
    S.allMovies = r.data;
    // Genre chips
    var genres = ['all'];
    r.data.forEach(function(m){ if(m.genre){ var gs=m.genre.split('/'); gs.forEach(function(g){ if(genres.indexOf(g)===-1)genres.push(g); }); } });
    $('genreChips').innerHTML = genres.map(function(g){
      return '<span class="chip'+(g==='all'?' active':'')+'" onclick="filterGenre(\''+escA(g)+'\',this)">'+(g==='all'?'全部':esc(g))+'</span>';
    }).join('');
    renderMovieGrid();
  });
}
function filterGenre(g, el) {
  S.currentGenre = g;
  document.querySelectorAll('#genreChips .chip').forEach(function(c){ c.classList.remove('active'); });
  if(el) el.classList.add('active');
  renderMovieGrid();
}
function searchMovies() {
  var kw = $('movieSearch').value.trim();
  if(kw){
    fetchJ(API+'/movies?keyword='+encodeURIComponent(kw)).then(function(r){
      if(r.success){ S.allMovies=r.data; S.currentGenre='all'; renderMovieGrid(); }
    });
  } else {
    loadMoviesPage();
  }
}
function sortMovies() { S.currentSort = $('movieSort').value; renderMovieGrid(); }
function renderMovieGrid() {
  var movies = S.allMovies.slice();
  if (S.currentGenre !== 'all') movies = movies.filter(function(m){ return m.genre && m.genre.indexOf(S.currentGenre)!==-1; });
  if (S.currentSort === 'rating') movies.sort(function(a,b){ return (b.rating||0)-(a.rating||0); });
  else if (S.currentSort === 'boxOffice') movies.sort(function(a,b){ return (b.boxOffice||0)-(a.boxOffice||0); });
  else if (S.currentSort === 'newest') movies.sort(function(a,b){ return (b.releaseDate||'').localeCompare(a.releaseDate||''); });
  $('movieGrid').innerHTML = movies.length ? movies.map(renderMovieCard).join('') : '<p style="color:var(--text-muted);text-align:center;grid-column:1/-1;padding:40px;">暂无影片</p>';
}
function renderMovieCard(m) {
  return '<div class="movie-card" onclick="showDetail('+m.id+')">'+
    '<div class="poster" style="background:'+(m.posterBg||'#1a1a2e')+';">'+
      (m.isHot?'<span class="badge-hot">热映</span>':'')+
      '<span class="badge-rating">★ '+(m.rating||0).toFixed(1)+'</span>'+
    '</div>'+
    '<div class="card-info"><h4>'+esc(m.title)+'</h4><div class="card-meta">'+(m.duration||'?')+'分钟 | '+esc(m.genre||'')+'</div></div>'+
  '</div>';
}

// ==================== Movie Detail ====================
function showDetail(id) {
  S.movieId = id;
  navTo('detail');
  fetchJ(API+'/movies/'+id).then(function(r){
    if(!r.success) return;
    var m = r.data;
    $('detailContent').innerHTML =
      '<div class="detail-hero" style="background:'+(m.posterBg||'#1a1a2e')+';">'+
        '<div class="detail-overlay"><h1>'+esc(m.title)+'</h1></div>'+
      '</div>'+
      '<div class="detail-info-grid">'+
        '<div>'+
          '<div class="detail-meta">'+
            '<span class="tag">★ '+(m.rating||0).toFixed(1)+'</span>'+
            '<span class="tag">'+esc(m.genre||'')+'</span>'+
            '<span class="tag">'+(m.duration||'?')+'分钟</span>'+
            '<span class="tag">'+esc(m.language||'')+'</span>'+
            '<span class="tag">'+esc(m.country||'')+'</span>'+
            '<span class="tag">导演: '+esc(m.director||'')+'</span>'+
            '<span class="tag">上映: '+esc(m.releaseDate||'')+'</span>'+
          '</div>'+
          '<p class="detail-desc">'+esc(m.description||'')+'</p>'+
          (m.cast?'<p class="detail-desc" style="color:var(--text-muted)">主演: '+esc(m.cast)+'</p>':'')+
          (m.boxOffice?'<p style="color:var(--accent2);font-size:13px;">累计票房: '+(m.boxOffice/100000000).toFixed(1)+'亿</p>':'')+
        '</div>'+
        '<div class="showtime-panel" id="showtimePanel"><p style="color:var(--text-muted);">正在加载场次...</p></div>'+
      '</div>';
    loadShowtimes(id);
    loadReviews(id);
  });
}
function loadShowtimes(movieId) {
  fetchJ(API+'/showtimes?movieId='+movieId).then(function(r){
    if(!r.success) return;
    S.allShowtimes = r.data || [];
    var dates = [];
    var seen = {};
    S.allShowtimes.forEach(function(s){ if(!seen[s.showDate]){ seen[s.showDate]=true; dates.push(s.showDate); } });
    dates.sort();
    if(!S.currentDate||dates.indexOf(S.currentDate)===-1) S.currentDate = dates[0]||'';
    var dHtml = dates.map(function(d){
      return '<span class="date-tab'+(d===S.currentDate?' active':'')+'" onclick="switchShowDate(\''+d+'\',this)">'+fmtDt(d)+'</span>';
    }).join('');
    $('showtimePanel').innerHTML = '<h3>🎫 选择场次</h3><div class="date-tabs">'+dHtml+'</div><div id="timeList"></div>';
    renderTimeList();
  });
}
function switchShowDate(d, el) {
  S.currentDate = d;
  document.querySelectorAll('.date-tab').forEach(function(t){ t.classList.remove('active'); });
  if(el) el.classList.add('active');
  renderTimeList();
}
function renderTimeList() {
  var filtered = S.allShowtimes.filter(function(s){ return s.showDate===S.currentDate; });
  // Group by hall
  var groups = {};
  filtered.forEach(function(s){
    var k = s.hallName||'';
    if(!groups[k]) groups[k]=[];
    groups[k].push(s);
  });
  var html = '';
  Object.keys(groups).forEach(function(k){
    html += '<p style="font-size:13px;color:var(--text-secondary);margin:8px 0 4px;">'+esc(k)+'</p>';
    groups[k].sort(function(a,b){ return a.showTime.localeCompare(b.showTime); });
    groups[k].forEach(function(s){
      html += '<div class="time-card" onclick="goSeats('+s.id+','+s.hallId+')">'+
        '<span class="t-time">'+esc(s.showTime)+'</span>'+
        '<span class="t-hall">'+esc(s.hallName||'')+'</span>'+
        '<span class="t-price">¥'+(s.priceStandard||0).toFixed(1)+' / VIP ¥'+(s.priceVip||0).toFixed(1)+'</span>'+
      '</div>';
    });
  });
  $('timeList').innerHTML = html || '<p style="color:var(--text-muted);text-align:center;padding:20px;">该日期暂无场次</p>';
}

// Reviews
function loadReviews(movieId) {
  fetchJ(API+'/reviews/movie/'+movieId).then(function(r){
    if(!r.success) return;
    var s = r.stats || {};
    var html = '<div style="padding:24px;"><h3 style="color:var(--accent);margin-bottom:12px;">💬 观众评价 '+
      (s.avgRating?'<span style="color:var(--accent2);">★ '+s.avgRating.toFixed(1)+'</span>':'')+
      ' <span style="font-size:13px;color:var(--text-muted);">('+(s.count||0)+'条)</span></h3>';
    if (isLogin()) {
      html += '<div style="margin-bottom:16px;display:flex;gap:8px;"><select id="reviewRating" class="select-glass"><option value="5">★★★★★</option><option value="4">★★★★</option><option value="3">★★★</option><option value="2">★★</option><option value="1">★</option></select>'+
        '<input type="text" id="reviewContent" placeholder="写下你的评价..." style="flex:1;padding:8px 12px;border:1px solid var(--border-glass);border-radius:8px;background:var(--bg-glass);color:var(--text-primary);outline:none;">'+
        '<button class="btn-glass" onclick="submitReview('+movieId+')">发表</button></div>';
    }
    if (r.data && r.data.length) {
      r.data.forEach(function(rv){
        html += '<div class="review-card"><div class="rv-header"><span class="rv-user">'+esc(rv.username||'匿名')+'</span><span class="rv-stars">'+'★'.repeat(rv.rating||0)+'</span></div>'+
          '<div class="rv-content">'+esc(rv.content||'')+'</div><div class="rv-time">'+esc(rv.createdAt||'')+'</div></div>';
      });
    } else { html += '<p style="color:var(--text-muted);">暂无评价，来做第一个评价吧</p>'; }
    html += '</div>';
    $('reviewSection').innerHTML = html;
  });
}
function submitReview(movieId) {
  var rating = +$('reviewRating').value, content = $('reviewContent').value.trim();
  if(!content){ toast('请输入评价内容','error'); return; }
  fetchJ(API+'/reviews',{method:'POST',headers:authHdrs(),body:JSON.stringify({movieId:movieId,rating:rating,content:content})}).then(function(r){
    if(r.success){ toast('评价成功！','success'); loadReviews(movieId); }
    else toast(r.message||'评价失败','error');
  });
}

// ==================== Seat Selection ====================
function goSeats(showtimeId, hallId) {
  S.showtimeId = showtimeId; S.hallId = hallId;
  S.selectedSeats = []; S.snackCart = {};
  navTo('seats');
  fetchJ(API+'/showtimes/'+showtimeId).then(function(r){ if(r.success) S.showtime = r.data; renderSeatsPage(); });
  fetchJ(API+'/seats?showtimeId='+showtimeId+'&hallId='+hallId).then(function(r){ S.seatData = r; renderSeatMap(); });
  fetchJ(API+'/snacks').then(function(r){ if(r.success) renderSnacksPanel(r.data); });
}
function renderSeatsPage() {
  $('seatsLayout').innerHTML =
    '<div class="seats-header"><button class="btn-glass" onclick="history.back()">← 返回</button><h3>选择座位 - '+(S.showtime?esc(S.showtime.hallName):'')+'</h3></div>'+
    '<div class="seats-legend">'+
      '<span class="legend-dot"><span class="l-avail"></span> 可选</span>'+
      '<span class="legend-dot"><span class="l-sel"></span> 已选</span>'+
      '<span class="legend-dot"><span class="l-booked"></span> 已售</span>'+
      '<span class="legend-dot"><span class="l-vip"></span> VIP</span>'+
      '<span class="legend-dot"><span class="l-couple"></span> 情侣座</span>'+
    '</div>'+
    '<div class="screen-bar">银 幕</div>'+
    '<div class="seat-map" id="seatMap"></div>'+
    '<div class="snacks-panel" id="snacksPanel"><h4>🍕 加购卖品</h4><div class="snacks-grid" id="snacksGrid"></div></div>'+
    '<div class="seats-summary"><span class="total">已选 <strong id="seatCount">0</strong> 座 | 合计 <strong id="seatTotal">¥0.00</strong></span>'+
      '<button class="btn-glass btn-primary" onclick="goCheckout()">去结算 →</button></div>';
}
function renderSeatMap() {
  if(!S.seatData) return;
  var rows = {};
  S.seatData.seats.forEach(function(s){
    if(!rows[s.rowLabel]) rows[s.rowLabel]=[];
    rows[s.rowLabel].push(s);
  });
  var html = '';
  Object.keys(rows).sort().forEach(function(rl){
    html += '<div class="seat-row"><span class="seat-row-label">'+esc(rl)+'</span>';
    rows[rl].sort(function(a,b){ return a.seatNum-b.seatNum; });
    rows[rl].forEach(function(s){
      var isBooked = s.booked;
      var isSel = S.selectedSeats.some(function(ss){ return ss.id===s.id; });
      var cls = 'seat';
      if(isBooked) cls+=' booked';
      else if(isSel) cls+=' sel';
      else cls+=' avail';
      if(s.seatType==='vip') cls+=' vip';
      if(s.seatType==='couple') cls+=' couple';
      var clickable = !isBooked;
      html += '<div class="'+cls+'"'+
        (clickable?' onclick="toggleSeat('+s.id+',\''+escA(s.rowLabel)+'\','+s.seatNum+',\''+escA(s.seatType)+'\')"':'')+
        ' title="'+escA(s.rowLabel)+'排'+s.seatNum+'座'+(s.seatType!=='standard'?' '+s.seatType.toUpperCase():'')+'">'+s.seatNum+'</div>';
    });
    html += '<span class="seat-row-label">'+esc(rl)+'</span></div>';
  });
  var el = $('seatMap'); if(el) el.innerHTML = html;
  updateSeatSummary();
}
function toggleSeat(id, row, num, type) {
  var idx = -1;
  for(var i=0;i<S.selectedSeats.length;i++){ if(S.selectedSeats[i].id===id){idx=i;break;} }
  if(idx>=0) S.selectedSeats.splice(idx,1);
  else S.selectedSeats.push({id:id,rowLabel:row,seatNum:num,seatType:type});
  renderSeatMap();
}
function updateSeatSummary() {
  var cnt = $('seatCount'); if(cnt) cnt.textContent = S.selectedSeats.length;
  var total = 0;
  if(S.showtime){
    S.selectedSeats.forEach(function(s){ total += s.seatType==='vip'? (S.showtime.priceVip||59.9) : (S.showtime.priceStandard||39.9); });
    Object.keys(S.snackCart).forEach(function(k){ var sc=S.snackCart[k]; total+=sc.price*sc.qty; });
  }
  var el = $('seatTotal'); if(el) el.textContent = '¥'+total.toFixed(2);
}
function renderSnacksPanel(snacks) {
  var html = '';
  snacks.forEach(function(s){
    var qty = S.snackCart[s.id] ? S.snackCart[s.id].qty : 0;
    html += '<div class="snack-chip'+(qty>0?' sel':'')+'" id="snack-'+s.id+'">'+
      '<span>'+esc(s.name)+' ¥'+(s.price||0).toFixed(1)+'</span>'+
      '<span class="sq" id="snack-qty-'+s.id+'">'+(qty>0?qty:'+')+'</span>'+
    '</div>';
  });
  var grid = $('snacksGrid'); if(grid) grid.innerHTML = html;
  snacks.forEach(function(s){
    var el = document.getElementById('snack-'+s.id);
    if(el) el.addEventListener('click', function(){ toggleSnack(s.id,s.name,s.price); });
  });
}
function toggleSnack(id, name, price) {
  if(!S.snackCart[id]) S.snackCart[id]={id:id,name:name,price:price,qty:0};
  S.snackCart[id].qty++;
  if(S.snackCart[id].qty>5) S.snackCart[id].qty=0;
  if(S.snackCart[id].qty===0) delete S.snackCart[id];
  renderSnacksPanel(S.snackCache||[]);
  updateSeatSummary();
}
function goCheckout() {
  if(S.selectedSeats.length===0){ toast('请先选择座位','error'); return; }
  if(!isLogin()){ toast('请先登录再下单','info'); showAuth(); return; }
  navTo('checkout');
  renderCheckout();
}

// ==================== Checkout ====================
function renderCheckout() {
  var seatTotal = 0, seatLines = [];
  S.selectedSeats.forEach(function(s){
    var p = s.seatType==='vip'? (S.showtime.priceVip||59.9) : (S.showtime.priceStandard||39.9);
    seatTotal += p;
    seatLines.push(s.rowLabel+'排'+s.seatNum+'座 ('+(s.seatType==='vip'?'VIP':'')+'¥'+p.toFixed(1)+')');
  });
  var snackTotal = 0, snackLines = [];
  Object.keys(S.snackCart).forEach(function(k){
    var sc=S.snackCart[k], sub=sc.price*sc.qty;
    snackTotal+=sub;
    snackLines.push(sc.name+' x'+sc.qty+' (¥'+sub.toFixed(1)+')');
  });
  var total = seatTotal + snackTotal;
  var html = '<div class="checkout-card"><h3>📋 确认订单</h3>'+
    '<div class="checkout-summary">'+
      '<div><span class="cl">影片：</span>'+esc(S.showtime.movieTitle||'')+'</div>'+
      '<div><span class="cl">影厅：</span>'+esc(S.showtime.hallName||'')+'</div>'+
      '<div><span class="cl">时间：</span>'+esc(S.showtime.showDate||'')+' '+esc(S.showtime.showTime||'')+'</div>'+
      '<div><span class="cl">座位：</span>'+seatLines.join('、')+'</div>'+
      (snackLines.length?'<div><span class="cl">卖品：</span>'+snackLines.join('、')+'</div>':'')+
      '<div class="ct"><span class="cl">合计：</span>¥'+total.toFixed(2)+'</div>'+
    '</div>'+
    '<div class="form-group"><label>姓名</label><input type="text" id="cfmName" placeholder="请输入姓名"></div>'+
    '<div class="form-group"><label>手机号</label><input type="text" id="cfmPhone" placeholder="请输入手机号"></div>'+
    '<div class="form-group"><label>支付方式</label>'+
      '<div class="payment-methods">'+
        '<div class="pay-method sel" data-pm="wechat" onclick="selPay(this)">微信</div>'+
        '<div class="pay-method" data-pm="alipay" onclick="selPay(this)">支付宝</div>'+
        '<div class="pay-method" data-pm="counter" onclick="selPay(this)">前台支付</div>'+
      '</div>'+
    '</div>'+
    '<button class="btn-glass btn-primary btn-full" onclick="submitBooking()" style="padding:14px;font-size:16px;">确认下单</button>'+
    '<button class="btn-glass btn-full" onclick="goSeats(S.showtimeId,S.hallId)" style="margin-top:8px;">返回修改</button>'+
    '</div>';
  $('checkoutContent').innerHTML = html;
}
function selPay(el) {
  document.querySelectorAll('.pay-method').forEach(function(pm){ pm.classList.remove('sel'); });
  el.classList.add('sel');
}

function submitBooking() {
  var name=$('cfmName').value.trim(), phone=$('cfmPhone').value.trim();
  if(!name){toast('请输入姓名','error');return;}
  if(!phone||!/^1\d{10}$/.test(phone)){toast('请输入正确手机号','error');return;}
  var pm = document.querySelector('.pay-method.sel');
  var payMethod = pm ? pm.getAttribute('data-pm') : 'counter';
  var seatIds = S.selectedSeats.map(function(s){ return s.id; });
  var snacksJson = '';
  var parts = [];
  Object.keys(S.snackCart).forEach(function(k){ var sc=S.snackCart[k]; if(sc.qty>0) parts.push(sc.id+':'+sc.qty); });
  snacksJson = parts.join(',');
  var body = {showtimeId:S.showtimeId, seatIds:seatIds, userName:name, userPhone:phone, snacksJson:snacksJson};
  fetchJ(API+'/bookings',{method:'POST',headers:authHdrs(),body:JSON.stringify(body)}).then(function(r){
    if(r.success){
      // Process payment
      fetchJ(API+'/bookings/pay',{method:'POST',headers:authHdrs(),body:JSON.stringify({bookingCode:r.bookingCode,paymentMethod:payMethod})}).then(function(pr){
        showSuccess(r);
      });
    } else { toast(r.message||'下单失败','error'); }
  });
}
function showSuccess(r) {
  navTo('success');
  $('successContent').innerHTML =
    '<div class="success-card">'+
      '<div class="icon-big">✅</div><h2>下单成功！</h2>'+
      '<p style="color:var(--text-muted);">请凭取票码到影院自助取票</p>'+
      '<div class="qr-code">'+esc(r.bookingCode)+'</div>'+
      '<div class="ticket-info">'+
        '<div><span class="cl">影片：</span>'+esc(r.movieTitle||'')+'</div>'+
        '<div><span class="cl">影厅：</span>'+esc(r.hallName||'')+'</div>'+
        '<div><span class="cl">时间：</span>'+esc(r.showDate||'')+' '+esc(r.showTime||'')+'</div>'+
        '<div><span class="cl">座位：</span>'+(r.seatLabels||[]).join('、')+'</div>'+
        '<div><span class="cl">金额：</span><strong style="color:var(--accent2);">¥'+r.totalPrice.toFixed(2)+'</strong></div>'+
      '</div>'+
      '<button class="btn-glass btn-primary btn-full" onclick="navTo(\'home\')">返回首页</button>'+
      (isLogin()?'<button class="btn-glass btn-full" onclick="navTo(\'user\')">查看我的订单</button>':'')+
    '</div>';
  S.selectedSeats=[]; S.snackCart={};
}

// ==================== User Center ====================
function loadUserCenter() {
  if(!isLogin()){ showAuth(); return; }
  navTo('user');
  fetchJ(API+'/user/profile',{headers:authHdrs()}).then(function(r){
    if(r.success&&r.data){
      var u=r.data;
      $('userCenterName').textContent=u.username;
      $('userCenterLevel').textContent='Lv.'+(u.memberLevel||0)+' 会员';
      $('userCenterPoints').textContent=u.points||0;
    }
  });
  switchUserTab('orders');
}
function switchUserTab(tab, el) {
  document.querySelectorAll('.user-nav').forEach(function(n){ n.classList.remove('active'); });
  if(el) el.classList.add('active');
  var c = $('userContent');
  if(tab==='orders') loadUserOrders(c);
  else if(tab==='reviews') loadUserReviews(c);
  else if(tab==='coupons') loadUserCoupons(c);
  else if(tab==='notifications') loadUserNotifications(c);
  else if(tab==='profile') loadUserProfile(c);
}
function loadUserOrders(c) {
  c.innerHTML = '<p style="color:var(--text-muted);">加载中...</p>';
  fetchJ(API+'/bookings/my',{headers:authHdrs()}).then(function(r){
    if(!r.success||!r.data||!r.data.length){ c.innerHTML='<div class="card"><p style="color:var(--text-muted);text-align:center;padding:40px;">暂无订单</p></div>'; return; }
    c.innerHTML = '<h3 style="margin-bottom:14px;color:var(--accent);">🎫 我的订单</h3>'+r.data.map(renderOrderCard).join('');
  });
}
function renderOrderCard(b) {
  var labels = b.seatLabels||[];
  return '<div class="order-card">'+
    '<div class="oc-header"><span class="oc-code">'+esc(b.bookingCode)+'</span><span class="oc-status '+b.status+'">'+b.status+'</span></div>'+
    '<div class="oc-body">'+
      '<div><span class="lbl">影片：</span>'+esc(b.movieTitle||'')+'</div>'+
      '<div><span class="lbl">影厅：</span>'+esc(b.hallName||'')+'</div>'+
      '<div><span class="lbl">时间：</span>'+esc(b.showDate||'')+' '+esc(b.showTime||'')+'</div>'+
      '<div><span class="lbl">座位：</span>'+labels.join('、')+'</div>'+
      (b.snacksJson?'<div><span class="lbl">卖品：</span>'+esc(b.snacksJson)+'</div>':'')+
      '<div><span class="lbl">金额：</span><strong style="color:var(--accent2);">¥'+(b.totalPrice||0).toFixed(2)+'</strong></div>'+
      '<div><span class="lbl">时间：</span>'+esc(b.createdAt||'')+'</div>'+
    '</div>'+
    (b.status==='confirmed'?'<button class="btn-glass-sm" style="margin-top:8px;color:#e74c3c;" onclick="cancelUserBooking(\''+escA(b.bookingCode)+'\')">取消订单</button>':'')+
  '</div>';
}
function cancelUserBooking(code) {
  if(!confirm('确定取消此订单？')) return;
  fetchJ(API+'/bookings/cancel',{method:'POST',headers:authHdrs(),body:JSON.stringify({bookingCode:code})}).then(function(r){
    if(r.success){ toast('已取消'); switchUserTab('orders'); } else toast(r.message,'error');
  });
}
function loadUserReviews(c) {
  c.innerHTML = '<p style="color:var(--text-muted);">加载中...</p>';
  fetchJ(API+'/reviews/my',{headers:authHdrs()}).then(function(r){
    if(!r.success||!r.data||!r.data.length){ c.innerHTML='<div class="card"><p style="color:var(--text-muted);text-align:center;padding:40px;">暂无评价</p></div>'; return; }
    c.innerHTML = '<h3 style="margin-bottom:14px;color:var(--accent);">💬 我的评价</h3>'+r.data.map(function(rv){
      return '<div class="review-card"><div class="rv-header"><span class="rv-stars">'+'★'.repeat(rv.rating||0)+'</span><span class="rv-time">'+esc(rv.createdAt||'')+'</span></div><div class="rv-content">'+esc(rv.content||'')+'</div></div>';
    }).join('');
  });
}
function loadUserCoupons(c) {
  c.innerHTML = '<p style="color:var(--text-muted);">加载中...</p>';
  fetchJ(API+'/coupons/my',{headers:authHdrs()}).then(function(r){
    if(!r.success||!r.data||!r.data.length){ c.innerHTML='<div class="card"><p style="color:var(--text-muted);text-align:center;padding:40px;">暂无优惠券</p></div>'; return; }
    c.innerHTML = '<h3 style="margin-bottom:14px;color:var(--accent);">🎟️ 我的优惠券</h3>'+r.data.map(function(uc){
      var cp = uc.coupon||{};
      return '<div class="coupon-card"><div class="cp-value">¥'+(cp.discountAmount||0)+'</div><div class="cp-info"><div class="cp-name">'+esc(cp.name||'优惠券')+'</div><div class="cp-cond">满'+(cp.minAmount||0).toFixed(1)+'可用</div></div></div>';
    }).join('');
  });
}
function loadUserNotifications(c) {
  c.innerHTML = '<p style="color:var(--text-muted);">加载中...</p>';
  fetchJ(API+'/notifications',{headers:authHdrs()}).then(function(r){
    if(!r.success||!r.data||!r.data.length){ c.innerHTML='<div class="card"><p style="color:var(--text-muted);text-align:center;padding:40px;">暂无消息</p></div>'; return; }
    c.innerHTML = '<h3 style="margin-bottom:14px;color:var(--accent);">🔔 消息通知 ('+(r.unreadCount||0)+'未读)</h3>'+r.data.map(function(n){
      return '<div class="notif-card'+(n.isRead?'':' unread')+'" onclick="readNotif('+n.id+')"><div class="nf-title">'+esc(n.title||'')+'</div><div class="nf-content">'+esc(n.content||'')+'</div><div class="nf-time">'+esc(n.createdAt||'')+'</div></div>';
    }).join('');
  });
}
function readNotif(id) {
  fetchJ(API+'/notifications/'+id+'/read',{method:'PUT',headers:authHdrs()}).then(function(){ loadUserNotifications($('userContent')); });
}
function loadUserProfile(c) {
  fetchJ(API+'/user/profile',{headers:authHdrs()}).then(function(r){
    if(!r.success){ c.innerHTML='<div class="card"><p>请先登录</p></div>'; return; }
    var u=r.data;
    c.innerHTML = '<div class="card"><h3 style="color:var(--accent);margin-bottom:16px;">⚙️ 账号设置</h3>'+
      '<div class="form-group"><label>用户名</label><input type="text" value="'+esc(u.username||'')+'" disabled></div>'+
      '<div class="form-group"><label>手机号</label><input type="text" id="pfPhone" value="'+esc(u.phone||'')+'"></div>'+
      '<div class="form-group"><label>邮箱</label><input type="text" id="pfEmail" value="'+esc(u.email||'')+'"></div>'+
      '<button class="btn-glass btn-primary" onclick="updateProfile()">保存修改</button>'+
      '<hr style="border-color:var(--border-glass);margin:20px 0;">'+
      '<h4 style="margin-bottom:10px;">修改密码</h4>'+
      '<div class="form-group"><label>旧密码</label><input type="password" id="pfOldPass"></div>'+
      '<div class="form-group"><label>新密码</label><input type="password" id="pfNewPass"></div>'+
      '<button class="btn-glass" onclick="changePassword()">修改密码</button>'+
      '</div>';
  });
}
function updateProfile() {
  var d={phone:$('pfPhone').value.trim(), email:$('pfEmail').value.trim()};
  fetchJ(API+'/user/profile',{method:'PUT',headers:authHdrs(),body:JSON.stringify(d)}).then(function(r){
    if(r.success) toast('资料已更新','success'); else toast(r.message,'error');
  });
}
function changePassword() {
  var o=$('pfOldPass').value.trim(), n=$('pfNewPass').value.trim();
  if(!o||!n){ toast('请填写密码','error'); return; }
  fetchJ(API+'/user/change-password',{method:'POST',headers:authHdrs(),body:JSON.stringify({oldPassword:o,newPassword:n})}).then(function(r){
    if(r.success) toast('密码已修改','success'); else toast(r.message,'error');
  });
}

// ==================== Admin ====================
function loadAdmin() {
  if(!isAdmin()){ navTo('home'); return; }
  navTo('admin');
  switchAdminTab('overview');
}
function switchAdminTab(tab, el) {
  document.querySelectorAll('.admin-nav').forEach(function(a){ a.classList.remove('active'); });
  if(el) el.classList.add('active');
  var c = $('adminContent');
  var tabs = {
    overview: loadAdminOverview, movies: loadAdminMovies, showtimes: loadAdminShowtimes,
    bookings: loadAdminBookings, snacks: loadAdminSnacks, coupons: loadAdminCoupons,
    cinemas: loadAdminCinemas, announcements: loadAdminAnnouncements, users: loadAdminUsers
  };
  if(tabs[tab]) tabs[tab](c);
}
function loadAdminOverview(c) {
  fetchJ(API+'/admin/stats/overview',{headers:authHdrs()}).then(function(r){
    if(!r.success) return;
    var html = '<h3>📊 数据总览</h3><div class="stats-grid">'+
      statCard('影片数',r.movieCount)+statCard('场次数',r.showtimeCount)+statCard('订单数',r.bookingCount)+
      statCard('影厅数',r.hallCount)+statCard('用户数',r.userCount)+statCard('影院数',r.cinemaCount)+
      statCard('营收(元)',(r.revenue||0).toFixed(2))+'</div>';
    if(r.topMovies && r.topMovies.length) {
      html += '<h4 style="margin-top:20px;margin-bottom:10px;">🏆 热门影片</h4><table class="admin-table"><tr><th>影片</th><th>类型</th><th>票房</th><th>评分</th></tr>';
      r.topMovies.forEach(function(m){ html += '<tr><td>'+esc(m.title||'')+'</td><td>'+esc(m.genre||'')+'</td><td>¥'+((m.boxOffice||0)/100000000).toFixed(1)+'亿</td><td>★ '+(m.rating||0).toFixed(1)+'</td></tr>'; });
      html += '</table>';
    }
    c.innerHTML = html;
  });
}
function statCard(l,n){ return '<div class="stat-card"><div class="num">'+n+'</div><div class="lbl">'+l+'</div></div>'; }

function loadAdminMovies(c) {
  fetchJ(API+'/admin/movies',{headers:authHdrs()}).then(function(r){
    var html = '<h3>🎬 影片管理</h3>'+
      '<div class="admin-form">'+
        '<input id="amTitle" placeholder="片名"><input id="amDirector" placeholder="导演">'+
        '<input id="amCast" placeholder="主演"><input id="amGenre" placeholder="类型">'+
        '<input id="amDuration" type="number" placeholder="时长(分钟)"><input id="amLanguage" placeholder="语言">'+
        '<input id="amReleaseDate" placeholder="上映日期"><input id="amCountry" placeholder="国家">'+
        '<input id="amPosterBg" placeholder="海报渐变"><input id="amPosterUrl" placeholder="海报URL">'+
        '<input id="amTrailerUrl" placeholder="预告片URL"><input id="amRating" type="number" step="0.1" placeholder="评分">'+
        '<input id="amBoxOffice" type="number" step="0.01" placeholder="票房"><textarea id="amDesc" placeholder="简介" rows="2"></textarea>'+
        '<div><label><input type="checkbox" id="amIsHot"> 热映</label></div>'+
        '<button class="btn-glass btn-primary" onclick="adminAddMovie()">添加影片</button>'+
      '</div>'+
      '<table class="admin-table"><tr><th>ID</th><th>片名</th><th>类型</th><th>评分</th><th>状态</th><th>操作</th></tr>';
    if(r.success && r.data) r.data.forEach(function(m){
      html += '<tr><td>'+m.id+'</td><td>'+esc(m.title)+'</td><td>'+esc(m.genre)+'</td><td>★ '+(m.rating||0).toFixed(1)+'</td><td>'+m.status+'</td>'+
        '<td><button class="btn-glass-sm" style="color:#e74c3c;" onclick="adminDelMovie('+m.id+')">删除</button></td></tr>';
    });
    html += '</table>';
    c.innerHTML = html;
  });
}
function adminAddMovie() {
  var m={};
  m.title=$('amTitle').value.trim(); m.director=$('amDirector').value.trim(); m.cast=$('amCast').value.trim();
  m.genre=$('amGenre').value.trim(); m.duration=+$('amDuration').value||120; m.language=$('amLanguage').value.trim();
  m.releaseDate=$('amReleaseDate').value.trim(); m.country=$('amCountry').value.trim();
  m.posterBg=$('amPosterBg').value.trim(); m.posterUrl=$('amPosterUrl').value.trim();
  m.trailerUrl=$('amTrailerUrl').value.trim(); m.rating=+$('amRating').value||0;
  m.boxOffice=+$('amBoxOffice').value||0; m.description=$('amDesc').value.trim();
  m.isHot=$('amIsHot').checked; m.status='showing';
  if(!m.title){ toast('请输入片名','error'); return; }
  fetchJ(API+'/admin/movies',{method:'POST',headers:authHdrs(),body:JSON.stringify(m)}).then(function(r){
    if(r.success){ toast('添加成功','success'); switchAdminTab('movies'); } else toast(r.message,'error');
  });
}
function adminDelMovie(id) {
  if(!confirm('确定删除？')) return;
  fetchJ(API+'/admin/movies/'+id,{method:'DELETE',headers:authHdrs()}).then(function(r){
    if(r.success){ toast('已删除'); switchAdminTab('movies'); } else toast(r.message,'error');
  });
}
function loadAdminShowtimes(c) {
  Promise.all([
    fetchJ(API+'/admin/showtimes',{headers:authHdrs()}),
    fetchJ(API+'/admin/movies',{headers:authHdrs()}),
    fetchJ(API+'/admin/halls',{headers:authHdrs()})
  ]).then(function(rs){
    var st=rs[0], mv=rs[1], hl=rs[2];
    var movOpts = (mv.data||[]).map(function(m){ return '<option value="'+m.id+'">'+esc(m.title)+'</option>'; }).join('');
    var hallOpts = (hl.data||[]).map(function(h){ return '<option value="'+h.id+'">'+esc(h.name)+'</option>'; }).join('');
    var html = '<h3>📅 场次管理</h3>'+
      '<div class="admin-form">'+
        '<select id="stMovieId">'+movOpts+'</select><select id="stHallId">'+hallOpts+'</select>'+
        '<input id="stDate" placeholder="日期(YYYY-MM-DD)"><input id="stTime" placeholder="时间(HH:MM)">'+
        '<input id="stPriceStd" type="number" step="0.1" value="39.9" placeholder="标准票价">'+
        '<input id="stPriceVip" type="number" step="0.1" value="59.9" placeholder="VIP票价">'+
        '<button class="btn-glass btn-primary" onclick="adminAddShowtime()">添加场次</button>'+
      '</div>'+
      '<table class="admin-table"><tr><th>ID</th><th>影片</th><th>影厅</th><th>日期</th><th>时间</th><th>操作</th></tr>';
    (st.data||[]).forEach(function(s){
      html += '<tr><td>'+s.id+'</td><td>'+esc(s.hallName)+'</td><td>'+esc(s.hallName)+'</td><td>'+s.showDate+'</td><td>'+s.showTime+'</td>'+
        '<td><button class="btn-glass-sm" style="color:#e74c3c;" onclick="adminDelShowtime('+s.id+')">删除</button></td></tr>';
    });
    html += '</table>';
    c.innerHTML = html;
  });
}
function adminAddShowtime() {
  var d={movieId:+$('stMovieId').value, hallId:+$('stHallId').value, showDate:$('stDate').value.trim(),
    showTime:$('stTime').value.trim(), priceStandard:+$('stPriceStd').value, priceVip:+$('stPriceVip').value};
  if(!d.showDate||!d.showTime){ toast('请填写日期和时间','error'); return; }
  fetchJ(API+'/admin/showtimes',{method:'POST',headers:authHdrs(),body:JSON.stringify(d)}).then(function(r){
    if(r.success){ toast('添加成功','success'); switchAdminTab('showtimes'); } else toast(r.message,'error');
  });
}
function adminDelShowtime(id) {
  if(!confirm('确定删除？')) return;
  fetchJ(API+'/admin/showtimes/'+id,{method:'DELETE',headers:authHdrs()}).then(function(r){
    if(r.success){ toast('已删除'); switchAdminTab('showtimes'); } else toast(r.message,'error');
  });
}
function loadAdminBookings(c) {
  fetchJ(API+'/admin/bookings',{headers:authHdrs()}).then(function(r){
    var html = '<h3>🎫 订单管理</h3><table class="admin-table"><tr><th>ID</th><th>取票码</th><th>影片</th><th>座位</th><th>姓名</th><th>金额</th><th>状态</th><th>操作</th></tr>';
    (r.data||[]).forEach(function(b){
      html += '<tr><td>'+b.id+'</td><td>'+esc(b.bookingCode)+'</td><td>'+esc(b.movieTitle)+'</td><td>'+(b.seatLabels||[]).join(',')+'</td>'+
        '<td>'+esc(b.userName)+'</td><td>¥'+(b.totalPrice||0).toFixed(2)+'</td><td>'+b.status+'</td>'+
        '<td>'+(b.status==='confirmed'?'<button class="btn-glass-sm" style="color:#e74c3c;" onclick="adminCancelBooking('+b.id+')">取消</button>':'')+'</td></tr>';
    });
    html += '</table>'; c.innerHTML = html;
  });
}
function adminCancelBooking(id) {
  if(!confirm('确定取消此订单？')) return;
  fetchJ(API+'/admin/bookings/'+id+'/cancel',{method:'PUT',headers:authHdrs()}).then(function(r){
    if(r.success){ toast('已取消'); switchAdminTab('bookings'); } else toast(r.message,'error');
  });
}
function loadAdminSnacks(c) {
  fetchJ(API+'/admin/snacks',{headers:authHdrs()}).then(function(r){
    var html = '<h3>🍕 卖品管理</h3>'+
      '<div class="admin-form">'+
        '<input id="snName" placeholder="名称"><input id="snCategory" placeholder="分类">'+
        '<input id="snPrice" type="number" step="0.1" placeholder="价格"><input id="snImage" placeholder="图标/颜色">'+
        '<button class="btn-glass btn-primary" onclick="adminAddSnack()">添加卖品</button>'+
      '</div>'+
      '<table class="admin-table"><tr><th>ID</th><th>名称</th><th>分类</th><th>价格</th><th>状态</th><th>操作</th></tr>';
    (r.data||[]).forEach(function(s){
      html += '<tr><td>'+s.id+'</td><td>'+esc(s.name)+'</td><td>'+esc(s.category)+'</td><td>¥'+(s.price||0).toFixed(1)+'</td><td>'+s.status+'</td>'+
        '<td><button class="btn-glass-sm" style="color:#e74c3c;" onclick="adminDelSnack('+s.id+')">删除</button></td></tr>';
    });
    html += '</table>'; c.innerHTML = html;
  });
}
function adminAddSnack() {
  var d={name:$('snName').value.trim(), category:$('snCategory').value.trim(), price:+$('snPrice').value||0, image:$('snImage').value.trim(), status:'on'};
  if(!d.name){ toast('请输入名称','error'); return; }
  fetchJ(API+'/admin/snacks',{method:'POST',headers:authHdrs(),body:JSON.stringify(d)}).then(function(r){
    if(r.success){ toast('添加成功','success'); switchAdminTab('snacks'); } else toast(r.message,'error');
  });
}
function adminDelSnack(id) {
  if(!confirm('确定删除？')) return;
  fetchJ(API+'/admin/snacks/'+id,{method:'DELETE',headers:authHdrs()}).then(function(r){
    if(r.success){ toast('已删除'); switchAdminTab('snacks'); } else toast(r.message,'error');
  });
}
function loadAdminCoupons(c) {
  fetchJ(API+'/admin/coupons',{headers:authHdrs()}).then(function(r){
    var html = '<h3>🎟️ 优惠券管理</h3>'+
      '<div class="admin-form">'+
        '<input id="cpName" placeholder="名称"><input id="cpDiscount" type="number" step="0.1" placeholder="优惠金额">'+
        '<input id="cpMinAmount" type="number" step="0.1" placeholder="最低消费"><input id="cpTotal" type="number" placeholder="发行数量">'+
        '<input id="cpExpire" placeholder="有效期(天)"><input id="cpType" placeholder="类型(full_discount)">'+
        '<button class="btn-glass btn-primary" onclick="adminAddCoupon()">添加优惠券</button>'+
      '</div>'+
      '<table class="admin-table"><tr><th>ID</th><th>名称</th><th>优惠</th><th>使用/总数</th><th>操作</th></tr>';
    (r.data||[]).forEach(function(cp){
      html += '<tr><td>'+cp.id+'</td><td>'+esc(cp.name)+'</td><td>¥'+(cp.discountAmount||0)+'</td><td>'+(cp.usedCount||0)+'/'+(cp.totalCount||0)+'</td>'+
        '<td><button class="btn-glass-sm" style="color:#e74c3c;" onclick="adminDelCoupon('+cp.id+')">删除</button></td></tr>';
    });
    html += '</table>'; c.innerHTML = html;
  });
}
function adminAddCoupon() {
  var d={name:$('cpName').value.trim(), discountAmount:+$('cpDiscount').value||0, minAmount:+$('cpMinAmount').value||0,
    totalCount:+$('cpTotal').value||100, expireDays:+$('cpExpire').value||30, type:$('cpType').value.trim()||'full_discount'};
  if(!d.name){ toast('请输入名称','error'); return; }
  fetchJ(API+'/admin/coupons',{method:'POST',headers:authHdrs(),body:JSON.stringify(d)}).then(function(r){
    if(r.success){ toast('添加成功','success'); switchAdminTab('coupons'); } else toast(r.message,'error');
  });
}
function adminDelCoupon(id) {
  if(!confirm('确定删除？')) return;
  fetchJ(API+'/admin/coupons/'+id,{method:'DELETE',headers:authHdrs()}).then(function(r){
    if(r.success){ toast('已删除'); switchAdminTab('coupons'); } else toast(r.message,'error');
  });
}
function loadAdminCinemas(c) {
  fetchJ(API+'/admin/cinemas',{headers:authHdrs()}).then(function(r){
    var html = '<h3>🏢 影院管理</h3>'+
      '<div class="admin-form">'+
        '<input id="cnName" placeholder="影院名称"><input id="cnAddress" placeholder="地址">'+
        '<input id="cnPhone" placeholder="电话"><input id="cnStatus" placeholder="状态(open)">'+
        '<button class="btn-glass btn-primary" onclick="adminAddCinema()">添加影院</button>'+
      '</div>'+
      '<table class="admin-table"><tr><th>ID</th><th>名称</th><th>地址</th><th>电话</th><th>状态</th><th>操作</th></tr>';
    (r.data||[]).forEach(function(cn){
      html += '<tr><td>'+cn.id+'</td><td>'+esc(cn.name)+'</td><td>'+esc(cn.address)+'</td><td>'+esc(cn.phone)+'</td><td>'+cn.status+'</td>'+
        '<td><button class="btn-glass-sm" style="color:#e74c3c;" onclick="adminDelCinema('+cn.id+')">删除</button></td></tr>';
    });
    html += '</table>'; c.innerHTML = html;
  });
}
function adminAddCinema() {
  var d={name:$('cnName').value.trim(), address:$('cnAddress').value.trim(), phone:$('cnPhone').value.trim(), status:$('cnStatus').value.trim()||'open'};
  if(!d.name){ toast('请输入名称','error'); return; }
  fetchJ(API+'/admin/cinemas',{method:'POST',headers:authHdrs(),body:JSON.stringify(d)}).then(function(r){
    if(r.success){ toast('添加成功','success'); switchAdminTab('cinemas'); } else toast(r.message,'error');
  });
}
function adminDelCinema(id) {
  if(!confirm('确定删除？')) return;
  fetchJ(API+'/admin/cinemas/'+id,{method:'DELETE',headers:authHdrs()}).then(function(r){
    if(r.success){ toast('已删除'); switchAdminTab('cinemas'); } else toast(r.message,'error');
  });
}
function loadAdminAnnouncements(c) {
  fetchJ(API+'/admin/announcements',{headers:authHdrs()}).then(function(r){
    var html = '<h3>📢 公告管理</h3>'+
      '<div class="admin-form">'+
        '<input id="anTitle" placeholder="标题"><textarea id="anContent" placeholder="内容" rows="2"></textarea>'+
        '<button class="btn-glass btn-primary" onclick="adminAddAnnouncement()">发布公告</button>'+
      '</div>'+
      '<table class="admin-table"><tr><th>ID</th><th>标题</th><th>内容</th><th>状态</th><th>操作</th></tr>';
    (r.data||[]).forEach(function(a){
      html += '<tr><td>'+a.id+'</td><td>'+esc(a.title)+'</td><td>'+esc(a.content||'')+'</td><td>'+a.status+'</td>'+
        '<td><button class="btn-glass-sm" style="color:#e74c3c;" onclick="adminDelAnnouncement('+a.id+')">删除</button></td></tr>';
    });
    html += '</table>'; c.innerHTML = html;
  });
}
function adminAddAnnouncement() {
  var d={title:$('anTitle').value.trim(), content:$('anContent').value.trim()};
  if(!d.title){ toast('请输入标题','error'); return; }
  fetchJ(API+'/admin/announcements',{method:'POST',headers:authHdrs(),body:JSON.stringify(d)}).then(function(r){
    if(r.success){ toast('已发布','success'); switchAdminTab('announcements'); } else toast(r.message,'error');
  });
}
function adminDelAnnouncement(id) {
  if(!confirm('确定删除？')) return;
  fetchJ(API+'/admin/announcements/'+id,{method:'DELETE',headers:authHdrs()}).then(function(r){
    if(r.success){ toast('已删除'); switchAdminTab('announcements'); } else toast(r.message,'error');
  });
}
function loadAdminUsers(c) {
  fetchJ(API+'/admin/users',{headers:authHdrs()}).then(function(r){
    var html = '<h3>👤 用户管理</h3><table class="admin-table"><tr><th>ID</th><th>用户名</th><th>角色</th><th>等级</th><th>积分</th><th>消费</th><th>手机</th><th>注册时间</th></tr>';
    (r.data||[]).forEach(function(u){
      html += '<tr><td>'+u.id+'</td><td>'+esc(u.username)+'</td><td>'+u.role+'</td><td>Lv.'+(u.memberLevel||0)+'</td>'+
        '<td>'+(u.points||0)+'</td><td>¥'+(u.totalSpent||0).toFixed(2)+'</td><td>'+esc(u.phone||'')+'</td><td>'+esc(u.createdAt||'')+'</td></tr>';
    });
    html += '</table>'; c.innerHTML = html;
  });
}

// ==================== Init ====================
updateUI();
loadCinemas();
(function(){
  var hash = (window.location.hash || '#home').replace('#', '');
  navTo(hash || 'home');
})();
