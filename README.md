# Proving Grounds of Werdna — CodinGame Multiplayer RPG SDK Project

Un puzzle multijoueur RPG tactique inspiré de **Wizardry: Proving Grounds of the Mad Overlord** et **D&D 5e (`RPG-web-arena`)**, avec bascule dynamique PixiJS entre **Mode Sprites RPG 2D** et **Mode Formes Géométriques Debug** (`ToggleModule`).

## Commandes Rapides
* **Viewer Web Local (`http://localhost:8888/test.html`)** :
  ```bash
  cd proving-grounds-werdna-sdk
  mvn test-compile exec:java -Dexec.mainClass="Main" -Dexec.classpathScope="test"
  ```
* **Simulation Batch (3 ligues)** :
  ```bash
  cd proving-grounds-werdna-sdk
  mvn test-compile exec:java -Dexec.mainClass="Main" -Dexec.classpathScope="test" -Dexec.args="--simulate"
  ```

## CodinGame & GitHub Links

- **CodinGame Contribution**: https://www.codingame.com/contribute/view/15867946f675bf476dc7f164eb3db1d6ce58f6
- **Play in CodinGame IDE**: https://www.codingame.com/ide/demo/1605773233912493b234f7c05aadee6e291708f
- **GitHub Repository**: https://github.com/codingame-team/codingame-proving-grounds-werdna
- **6-Puzzle Showcase Hub (GitHub Pages)**: https://codingame-team.github.io/codingame-sdk-puzzles/
