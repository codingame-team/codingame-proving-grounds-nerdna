import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { TooltipModule } from './tooltip-module/TooltipModule.js';
import { EndScreenModule } from './endscreen-module/EndScreenModule.js';
import { ToggleModule } from './toggle-module/ToggleModule.js';

export const modules = [
    GraphicEntityModule,
    TooltipModule,
    EndScreenModule,
    ToggleModule
];

export const playerColors = [
    '#ff1d5c', // Player 0: Crimson Fellowship
    '#22a1e4'  // Player 1: Azure Covenant
];

export const gameName = 'ProvingGroundsWerdna';
export const overlayAlpha = 0;

export const options = [
    ToggleModule.defineToggle({
        toggle: 'renderMode',
        title: 'RENDER MODE',
        values: {
            'RPG SPRITES': true,
            'GEOMETRIC DEBUG': false
        },
        default: true
    })
];
