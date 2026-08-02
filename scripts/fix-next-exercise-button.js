// Move Next Exercise button between muscle recovery bar and GIF, and make it compact (~40px height)
const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/MainActivity.kt';
let t = fs.readFileSync(path, 'utf8');

// ---------- EDIT 1: Remove the OLD button item block (at the end of the list) ----------
const oldButtonBlock = `            if (onNextExercise != null) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    LiquidNextExerciseButton(
                        text = strings.nextExercise,
                        onClick = { onNextExercise() },
                        isDark = isDark
                    )
                }
            }
`;
if (!t.includes(oldButtonBlock)) {
  console.error('ERROR: old button block not found');
  process.exit(1);
}
t = t.replace(oldButtonBlock, '');
console.log('Removed old button block from the end of the list');

// ---------- EDIT 2: Insert the compact button between recovery bar and GIF ----------
const gifAnchor = `            item {
                val gifUrl = ExerciseGifs.getGif(exercise.nume)
                if (gifUrl != null) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
`;
if (!t.includes(gifAnchor)) {
  console.error('ERROR: gif anchor not found');
  process.exit(1);
}
const insertBlock = `            if (onNextExercise != null) {
                item {
                    LiquidNextExerciseButton(
                        text = strings.nextExercise,
                        onClick = { onNextExercise() },
                        isDark = isDark
                    )
                }
            }
            item {
                val gifUrl = ExerciseGifs.getGif(exercise.nume)
                if (gifUrl != null) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
`;
t = t.replace(gifAnchor, insertBlock);
console.log('Inserted compact button block before the GIF item');

// ---------- EDIT 3: Make LiquidNextExerciseButton compact (~40px) ----------
// 3a: Outer modifier - reduce padding
const oldOuter = `    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .scale(buttonScale)
            .clip(RoundedCornerShape(16.dp))
            .background(buttonBg)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                tapTrigger++
                onClick()
            }
    ) {`;
const newOuter = `    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .scale(buttonScale)
            .clip(RoundedCornerShape(22.dp))
            .background(buttonBg)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                tapTrigger++
                onClick()
            }
    ) {`;
if (!t.includes(oldOuter)) {
  console.error('ERROR: outer modifier block not found');
  process.exit(1);
}
t = t.replace(oldOuter, newOuter);
console.log('Compact outer modifier applied');

// 3b: Row - fixed 40dp height, centered
const oldRow = `        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text.uppercase(),
                color = buttonText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(14.dp))

            Box(contentAlignment = Alignment.CenterStart) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = redPrimary.copy(alpha = ghostAlpha.value),
                    modifier = Modifier
                )
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = redPrimary,
                    modifier = Modifier.offset {
                        IntOffset(
                            x = arrowOffset.value.dp.roundToPx(),
                            y = 0
                        )
                    }
                )
            }
        }`;
const newRow = `        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text.uppercase(),
                color = buttonText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(contentAlignment = Alignment.CenterStart) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = redPrimary.copy(alpha = ghostAlpha.value),
                    modifier = Modifier.size(18.dp)
                )
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = redPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .offset {
                            IntOffset(
                                x = arrowOffset.value.dp.roundToPx(),
                                y = 0
                            )
                        }
                )
            }
        }`;
if (!t.includes(oldRow)) {
  console.error('ERROR: row block not found');
  process.exit(1);
}
t = t.replace(oldRow, newRow);
console.log('Compact row (40dp height) applied');

fs.writeFileSync(path, t, 'utf8');
console.log('DONE');
