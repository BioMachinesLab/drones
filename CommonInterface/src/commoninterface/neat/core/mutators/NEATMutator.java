/*
 * Created on 20-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package commoninterface.neat.core.mutators;

import java.util.ArrayList;
import java.util.Random;

import commoninterface.neat.core.InnovationDatabase;
import commoninterface.neat.core.NEATChromosome;
import commoninterface.neat.core.NEATFeatureGene;
import commoninterface.neat.core.NEATLinkGene;
import commoninterface.neat.core.NEATNeuron;
import commoninterface.neat.core.NEATNodeGene;
import commoninterface.neat.ga.core.Chromosome;
import commoninterface.neat.ga.core.Gene;
import commoninterface.neat.ga.core.Mutator;
import commoninterface.neat.utils.MathUtils;

/**
 * @author MSimmerson
 *
 * Performs all Link and node mutations
 */
public class NEATMutator implements Mutator {

    private double pAddLink;
    private double pAddNode;
    private double pPerturb;
    private double pToggle;
    private double pWeightReplaced;
    private double pMutateBias;
    private boolean featureSelection = false;
    private boolean recurrencyAllowed = true;
    private double perturb = 5;
    private double biasPerturb = 0.1;
    private InnovationDatabase db;
    private static final int MAX_LINK_ATTEMPTS = 5;
    private final Random linkRand = new Random();
    private final Random nodeRand = new Random(linkRand.nextLong());
    private final Random perturbRand = new Random(linkRand.nextLong());
    private final Random disableRand = new Random(linkRand.nextLong());

    public NEATMutator() {
    }

    public NEATMutator(double pAddNode, double pAddLink, double pDisable) {
        this.pAddNode = pAddNode;
        this.pAddLink = pAddLink;
        this.pToggle = pDisable;
    }

    public void setInnovationDatabase(InnovationDatabase db) {
        this.db = db;
    }

    public void setRecurrencyAllowed(boolean allowed) {
        this.recurrencyAllowed = allowed;
    }

    public void setProbability(double prob) {
        this.pPerturb = prob;
    }

    public void setPWeightReplaced(double pWR) {
        this.pWeightReplaced = pWR;
    }

    /**
     * Mutates the chromsome based on the set of probabilities.
     */
    public Chromosome mutate(Chromosome mutatee) {
        Gene[] genes = mutatee.genes();
        int originalSize = genes.length;
        NEATChromosome mutated;
        int i;
        for (i = 0; i < genes.length; i++) {
            if (genes[i] instanceof NEATLinkGene) {
                genes[i] = this.mutateLink((NEATLinkGene) genes[i]);
            } else if (genes[i] instanceof NEATNodeGene) {
                genes[i] = this.mutateNode((NEATNodeGene) genes[i]);
            } else if (genes[i] instanceof NEATFeatureGene) {
                genes[i] = this.mutateFeature((NEATFeatureGene) genes[i]);
            }
        }
        mutated = new NEATChromosome(genes);
        mutated.setSpecieId(((NEATChromosome) mutatee).getSpecieId());
        this.mutateAddLink(mutated);
        this.mutateAddNode(mutated);

        // now update chrome for depth and recurrency legality
        this.updateDepthInfo(mutated);
        mutated.updateChromosome(this.ensureLegalLinks(mutated.genes()));

        if (mutated.genes().length < originalSize) {
            System.out.println("Mutation -- Original: " + originalSize + " new: " + mutated.genes().length);
        }

        return (mutated);
    }

    private Gene mutateFeature(NEATFeatureGene mutatee) {
        double perturbRandVal = perturbRand.nextDouble();
        Gene mutated = mutatee;
        if (perturbRandVal < this.pPerturb) {
            mutated = new NEATFeatureGene(mutatee.getInnovationNumber(), mutatee.geneAsNumber().doubleValue() + MathUtils.nextClampedDouble(-perturb, perturb));
        }

        return (mutated);
    }

    private Gene mutateLink(NEATLinkGene mutatee) {
        double perturbRandVal = perturbRand.nextDouble();
        double disableRandVal = disableRand.nextDouble();
        double newWeight;
        NEATLinkGene mutated = mutatee;

        if (perturbRandVal < this.pPerturb) {
            if (this.pWeightReplaced > perturbRand.nextDouble()) {
                newWeight = MathUtils.nextPlusMinusOne();
            } else {
                newWeight = mutatee.getWeight() + MathUtils.nextClampedDouble(-perturb, perturb);
            }
//			newWeight = mutatee.getWeight() + MathUtils.nextClampedDouble(-PERTURB, PERTURB);				
            mutated = new NEATLinkGene(mutatee.getInnovationNumber(),
                    mutatee.isEnabled(),
                    mutatee.getFromId(),
                    mutatee.getToId(),
                    newWeight);
        }

        if (disableRandVal < this.pToggle) {
            if (this.featureSelection) {
                mutated.setEnabled(!mutated.isEnabled());
            }
        }

        return (mutated);
    }

    private Gene mutateNode(NEATNodeGene mutatee) {
        double perturbRandVal = perturbRand.nextDouble();
        double mutateBias = perturbRand.nextDouble();
        NEATNodeGene mutated = mutatee;
        double newSF = mutatee.sigmoidFactor();
        double newBias = mutatee.bias();

        if (perturbRandVal < this.pPerturb) {
            newSF = mutatee.sigmoidFactor() + MathUtils.nextClampedDouble(-perturb, perturb);
            mutated = new NEATNodeGene(mutated.getInnovationNumber(), mutated.id(), newSF, mutated.getType(), mutated.bias());
        }

        if (mutateBias < this.pMutateBias) {
            newBias += MathUtils.nextClampedDouble(-biasPerturb, biasPerturb);
            mutated = new NEATNodeGene(mutated.getInnovationNumber(), mutated.id(), mutated.sigmoidFactor(), mutated.getType(), newBias);
        }

        return (mutated);
    }

    private boolean linkIllegal(NEATNodeGene from, NEATNodeGene to, ArrayList links) {
        boolean illegal = false;
        int idx = 0;
        NEATLinkGene linkGene;

        if ((to.getType() == NEATNodeGene.INPUT)) {
            illegal = true;
        } else {
            while (!illegal && (idx < links.size())) {
                linkGene = (NEATLinkGene) links.get(idx);
//				if ((linkGene.getFromId() == from.id() && linkGene.getToId() == to.id()) || ((to.getDepth() <= from.getDepth()) && !this.recurrencyAllowed)) {
                if ((linkGene.getFromId() == from.id() && linkGene.getToId() == to.id())) {
                    illegal = true;
                }
                idx++;
            }
        }

        return (illegal);
    }

    private void mutateAddLink(Chromosome mutatee) {
        double linkRandVal = linkRand.nextDouble();
        NEATNodeGene from;
        NEATNodeGene to;
        int rIdx;
        int i = 0;
        ArrayList links;
        ArrayList nodes;
        Gene[] genes = new Gene[mutatee.size() + 1];
        System.arraycopy(mutatee.genes(), 0, genes, 0, mutatee.genes().length);
        Gene newLink = null;

        if (linkRandVal < this.pAddLink) {
            nodes = this.candidateNodes(mutatee.genes());
            links = this.candidateLinks(mutatee.genes(), false);
            // find a new available link
            while (newLink == null && i < MAX_LINK_ATTEMPTS) {
                rIdx = linkRand.nextInt(nodes.size());
                from = ((NEATNodeGene) nodes.get(rIdx));
                rIdx = linkRand.nextInt(nodes.size());
                to = ((NEATNodeGene) nodes.get(rIdx));
                if (!this.linkIllegal(from, to, links)) {
                    // set it to a random value
                    newLink = db.submitLinkInnovation(from.id(), to.id());
                    ((NEATLinkGene) newLink).setWeight(MathUtils.nextPlusMinusOne());
                    // add link between 2 unconnected nodes
                    genes[genes.length - 1] = newLink;
                    mutatee.updateChromosome(genes);
                }
                i++;
            }
        }
    }

    private void mutateAddNode(Chromosome mutatee) {
        double nodeRandVal = nodeRand.nextDouble();
        ArrayList nodeLinks;
        //ArrayList nodes;
        NEATLinkGene chosen;
        NEATNodeGene newNode;
        NEATLinkGene newLower;
        NEATLinkGene newUpper;
        int newChromoIdx = mutatee.genes().length;
        //Gene[] newChromo = new Gene[newChromoIdx + 3];
        Gene[] newChromo = new Gene[newChromoIdx + 2];
        System.arraycopy(mutatee.genes(), 0, newChromo, 0, newChromoIdx);
        int linkIdx;

        if (nodeRandVal < this.pAddNode) {
            // add a node on an existing enabled connection
            // find an existing connection to intercept
            nodeLinks = this.candidateLinks(mutatee.genes(), true);
            if (nodeLinks.size() > 0) {
                // ensure there is a link to split
                linkIdx = nodeRand.nextInt(nodeLinks.size());
                chosen = (NEATLinkGene) nodeLinks.get(linkIdx);
                // disable old link
                chosen.setEnabled(false);
                newNode = db.submitNodeInnovation(chosen);
                //newNode.setBias(MathUtils.nextPlusMinusOne());
                newLower = db.submitLinkInnovation(chosen.getFromId(), newNode.id());
                newUpper = db.submitLinkInnovation(newNode.id(), chosen.getToId());
                // set weights according to Stanley et al's NEAT document
                newLower.setWeight(1);
                newUpper.setWeight(chosen.getWeight());
                // now update the chromosome with new node and 2 new links
                newChromo[this.findChosenIndex(chosen, mutatee)] = newNode;
                //newChromo[newChromoIdx++] = newNode;
                newChromo[newChromoIdx++] = newLower;
                newChromo[newChromoIdx] = newUpper;
                mutatee.updateChromosome(newChromo);

            }
        }
    }

    private void updateDepthInfo(Chromosome mutated) {
        // use descriptor's chromo to create net 
        ArrayList nodes = new ArrayList();
        ArrayList links = new ArrayList();
        int i;
        Gene[] genes = mutated.genes();

        for (i = 0; i < genes.length; i++) {
            if (genes[i] instanceof NEATNodeGene) {
                nodes.add(genes[i]);
            } else if (genes[i] instanceof NEATLinkGene) {
                if (((NEATLinkGene) genes[i]).isEnabled()) {
                    // only add enabled links to the net structure
                    links.add(genes[i]);
                }
            }
        }

        this.assignNeuronDepth(this.findOutputNodes(this.candidateNodes(genes)), 1, mutated);
    }

    private void assignNeuronDepth(Gene[] nodeGenes, int depth, Chromosome mutated) {
        int i;
        NEATNodeGene node;
        //ArrayList nodeGenes = this.findOutputNodes(this.candidateNodes(genes));

        for (i = 0; i < nodeGenes.length; i++) {
            node = (NEATNodeGene) nodeGenes[i];
            if (node.getType() == NEATNodeGene.OUTPUT) {
                if (depth == 1) {
                    node.setDepth(depth);
                    this.assignNeuronDepth(this.findSourceNodes(node.id(), mutated.genes()), depth + 1, mutated);
                }
            } else if (node.getType() == NEATNodeGene.HIDDEN) {
                if (node.getDepth() == 0) {
                    // we have an unassigned depth
                    node.setDepth(depth);
                    this.assignNeuronDepth(this.findSourceNodes(node.id(), mutated.genes()), depth + 1, mutated);
                }
            } else if (node.getType() == NEATNodeGene.INPUT) {
                node.setDepth(Integer.MAX_VALUE);
            }
        }
    }

    private Gene[] findSourceNodes(int nodeId, Gene[] genes) {
        Gene[] sourceNodes = null;
        ArrayList links = this.candidateLinks(genes, true);
        NEATLinkGene link;
        ArrayList sources = new ArrayList();
        int i;

        for (i = 0; i < links.size(); i++) {
            link = (NEATLinkGene) links.get(i);
            if (nodeId == link.getToId()) {
                // add from Id
                sources.add(this.findNode(link.getFromId(), genes));
            }
        }

        sourceNodes = new NEATNodeGene[sources.size()];
        for (i = 0; i < sourceNodes.length; i++) {
            sourceNodes[i] = (NEATNodeGene) sources.get(i);
        }

        return (sourceNodes);
    }

    private Gene[] findOutputNodes(ArrayList nodes) {
        ArrayList outputNodes = new ArrayList();
        Gene[] nodeGenes;
        NEATNodeGene node;
        int i;

        for (i = 0; i < nodes.size(); i++) {
            node = (NEATNodeGene) nodes.get(i);
            if (node.getType() == NEATNodeGene.OUTPUT) {
                outputNodes.add(node);
            }
        }

        nodeGenes = new NEATNodeGene[outputNodes.size()];
        for (i = 0; i < nodeGenes.length; i++) {
            nodeGenes[i] = (NEATNodeGene) outputNodes.get(i);
        }

        return (nodeGenes);
    }

    private int findChosenIndex(NEATLinkGene chosen, Chromosome mutatee) {
        int idx = -1;
        int i = 0;
        Gene[] genes = mutatee.genes();
        int mutateeSize = genes.length;

        while (i < mutateeSize && idx == -1) {
            if (genes[i] instanceof NEATLinkGene
                    && ((NEATLinkGene) genes[i]).getFromId() == chosen.getFromId()
                    && ((NEATLinkGene) genes[i]).getToId() == chosen.getToId()) {
                idx = i;
            } else {
                i++;
            }
        }

        return (idx);
    }

    private NEATNodeGene findNode(int id, Gene[] genes) {
        int i = 0;
        Gene gene;
        NEATNodeGene node = null;
        boolean found = false;

        while (i < genes.length && !found) {
            gene = genes[i];
            if (gene instanceof NEATNodeGene) {
                node = (NEATNodeGene) genes[i];
                if (node.id() == id) {
                    found = true;
                }
            }
            i++;
        }

        return (node);
    }

    private ArrayList candidateLinks(Gene[] genes, boolean statusImportant) {
        ArrayList nodeLinks = new ArrayList();
        Gene gene;
        int i;

        for (i = 0; i < genes.length; i++) {
            gene = genes[i];
            if (gene instanceof NEATLinkGene) {
//				if (!statusImportant || (statusImportant && ((NEATLinkGene)gene).isEnabled() && ((NEATLinkGene)gene).getFromId() != ((NEATLinkGene)gene).getToId())) {
                if (!statusImportant || (statusImportant && ((NEATLinkGene) gene).isEnabled())) {
                    nodeLinks.add(gene);
                }
            }
        }

        return (nodeLinks);
    }

    private ArrayList candidateNodes(Gene[] genes) {
        ArrayList nodes = new ArrayList();
        Gene gene;
        int i;

        for (i = 0; i < genes.length; i++) {
            gene = genes[i];
            if (gene instanceof NEATNodeGene) {
                nodes.add(gene);
            }
        }

        return (nodes);
    }

    private Gene[] ensureLegalLinks(Gene[] genes) {
        ArrayList links;
        NEATLinkGene link;
        NEATNodeGene from;
        NEATNodeGene to;
        Gene[] newGenes = null;
        ArrayList tmpGenes = new ArrayList();
        int i;

        // only need to prune if recurrency not allowed
        if (!this.recurrencyAllowed) {
            // only return enabled links
            links = this.candidateLinks(genes, false);
            for (i = 0; i < genes.length; i++) {
                if (genes[i] instanceof NEATLinkGene) {
                    link = (NEATLinkGene) genes[i];
                    from = this.findNode(link.getFromId(), genes);
                    to = this.findNode(link.getToId(), genes);
                    if (from.getDepth() > to.getDepth()) {
                        // not recurrent - so keep
                        tmpGenes.add(genes[i]);
                    }
                } else {
                    tmpGenes.add(genes[i]);
                }
            }
            newGenes = new Gene[tmpGenes.size()];
            for (i = 0; i < newGenes.length; i++) {
                newGenes[i] = (Gene) tmpGenes.get(i);
            }
        } else {
            newGenes = genes;
        }

        return (newGenes);
    }

    /**
     * @param addLink The pAddLink to set.
     */
    public void setPAddLink(double addLink) {
        pAddLink = addLink;
    }

    /**
     * @param addNode The pAddNode to set.
     */
    public void setPAddNode(double addNode) {
        pAddNode = addNode;
    }

    /**
     * @param disable The pDisable to set.
     */
    public void setPToggle(double toggle) {
        pToggle = toggle;
    }

    /**
     * @param perturb The pPerturb to set.
     */
    public void setPPerturb(double perturb) {
        pPerturb = perturb;
    }

    public void setFeatureSelection(boolean featureSelection) {
        this.featureSelection = featureSelection;
    }

    public void setPMutateBias(double mutateBias) {
        pMutateBias = mutateBias;
    }

    public void setBiasPerturb(double biasPerturb) {
        this.biasPerturb = biasPerturb;
    }

    public void setPerturb(double perturb) {
        this.perturb = perturb;
    }
}
