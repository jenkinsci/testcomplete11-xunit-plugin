var PieChart = function(canvasId, data)
{
  if (data == null || data.length == 0)
    return;

  var canvas = document.getElementById(canvasId);

  if (canvas == null || typeof(canvas.getContext) === 'undefined')
    return;

  var total = 0;
  for (var i = 0; i < data.length; i++)
    total += data[i].value;

  if (total == 0)
    return;

  var ctx = canvas.getContext('2d');
  ctx.font = '13px Tahoma';
  ctx.globalAlpha = 1.0;
  ctx.lineJoin = "round";

  var radius = 75;
  var center = { "x": 130, "y": 100 };

  var sofar = 0;
  for (var i = 0; i < data.length; i++)
  {
    var thisvalue = data[i].value / total;
    var text = Math.round(thisvalue * 100) + "%";

    // sector

    var outline_color = this.rgbToRgba(this.lightenRgb(this.hexToRgb(data[i].color), 0.75), 0.8);

    ctx.beginPath();
    ctx.moveTo(center.x, center.y);
    ctx.arc(center.x, center.y, radius, -Math.PI * (2 * sofar), -Math.PI * (2 * (sofar + thisvalue)), true);
    ctx.lineTo(center.x, center.y);
    ctx.strokeStyle = outline_color;
    ctx.closePath();
    ctx.fillStyle = data[i].color;
    ctx.fill();
    ctx.stroke();

    // text

    if (data[i].value > 0)
    {
      var cos = Math.cos(-Math.PI * (2 * sofar + thisvalue));
      var sin = Math.sin(-Math.PI * (2 * sofar + thisvalue));
      ctx.beginPath();
      ctx.moveTo(center.x + radius * cos, center.y + radius * sin);
      ctx.lineTo(center.x + (radius + 20) * cos + ((cos > 0) ? -8 : 8), center.y + (radius + 20) * sin);
      ctx.lineTo(center.x + (radius + 20) * cos + ((cos > 0) ? -3 : 3), center.y + (radius + 20) * sin);
      ctx.strokeStyle = "#555555";
      ctx.stroke();

      var text_width = ctx.measureText(text).width;
      ctx.fillStyle = "#555555";
      ctx.fillText(text, center.x + (radius + 20) * cos + ((cos > 0) ? 2 : -2 - text_width), center.y + (radius + 20) * sin + 4);
    }

    sofar += thisvalue;

    this.draw_legend(ctx, 285, 30 + i * 45, data[i].color, data[i].label1, data[i].label2);
  }
}

PieChart.prototype.hexToRgb = function(hex)
{
  var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result ? {
    "r": parseInt(result[1], 16),
    "g": parseInt(result[2], 16),
    "b": parseInt(result[3], 16)
  } : null;
}

PieChart.prototype.componentToHex = function(c)
{
  var hex = c.toString(16);
  return hex.length == 1 ? "0" + hex : hex;
}

PieChart.prototype.rgbToHex = function(rgb)
{
  var hex = "#";
  for (var c in rgb)
    hex += this.componentToHex(rgb[c]);
  return hex;
}

PieChart.prototype.rgbToRgba = function(rgb, alpha)
{
  var rgba = "rgba(";
  for (var c in rgb)
    rgba += rgb[c] + ",";
  rgba += alpha + ")";
  return rgba;
}

PieChart.prototype.lightenRgb = function(rgb, percent)
{
  for (var c in rgb)
    rgb[c] = rgb[c] + Math.round((255 - rgb[c]) * percent);
  return rgb;
}

PieChart.prototype.darkenRgb = function(rgb, percent)
{
  for (var c in rgb)
    rgb[c] = Math.round(rgb[c] * percent);
  return rgb;
}

PieChart.prototype.draw_legend = function(ctx, x, y, bgColor, text1, text2)
{
  var lineColor = this.rgbToHex(this.darkenRgb(this.hexToRgb(bgColor), 0.6));
  var gradientColor = this.rgbToHex(this.lightenRgb(this.hexToRgb(bgColor), 0.6));

  ctx.save();

  ctx.fillStyle = bgColor;
  ctx.strokeStyle = lineColor;
  ctx.lineWidth = 0.5;

  var grd = ctx.createLinearGradient(x + 10, 0, x + 38, 0);
  grd.addColorStop(0, bgColor);
  grd.addColorStop(0.3, bgColor);
  grd.addColorStop(0.6, gradientColor);
  grd.addColorStop(0.9, bgColor);
  grd.addColorStop(1, bgColor);

  ctx.fillStyle = grd;
  ctx.beginPath();
  ctx.arc(x + 24, y + 24, 15, 0, 2 * Math.PI, false);
  ctx.moveTo(x + 24, y + 24);
  ctx.lineTo(x + 29.6, y + 10);
  ctx.moveTo(x + 24, y + 24);
  ctx.lineTo(x + 10, y + 24);
  ctx.moveTo(x + 24, y + 24);
  ctx.lineTo(x + 29.6, y + 38);
  ctx.closePath();
  ctx.fill();
  ctx.stroke();

  ctx.fillStyle = "#555555";
  ctx.fillText(text1, x + 48, y + 20);
  ctx.fillText(text2, x + 48, y + 36);

  ctx.restore();
}
