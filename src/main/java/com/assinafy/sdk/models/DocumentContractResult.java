package com.assinafy.sdk.models;

/** A document's contract terms or its link as an amendment to another contract. */
public sealed interface DocumentContractResult permits DocumentContract, DocumentContractAmendment {}
